package com.epam.search.services.impl;

import com.epam.search.common.JsonHelper;
import com.epam.search.domain.EventsPage;
import com.epam.search.services.SyncService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.search.common.ErrorUtil.tryable;
import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class RestSyncService implements SyncService {
    private static final String API_KEY = "bjDPa4wVqDyS1xXY6ASc2S4DGSxpmTNd";
    private static final String FILES_LOCATION = "D:\\search\\";
    public static final String INDEX_NAME = "discovery";
    public static final String EVENT_TYPE = "event";
    private int count = 0;

    private String buildRequest(Integer number) {
        return "https://app.ticketmaster.com/discovery/v1/events.json?"
                + "apikey=" + API_KEY + "&size=1000" + "&page=" + number;
    }

    private InputStream executeRequest(String url) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            error(null, e);
        }
        return null;
    }

    private void saveToFile(String fileName, InputStream stream) throws IOException {
        Path targetPath = new File(fileName).toPath();
        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        stream.close();
    }

    public void load() {
        tryable(() -> {
            load(0);
        });
        count = 0;
    }

    private boolean fileExist(String folder, int pageNumber) {
        return new File(buildFilePath(folder, pageNumber)).exists();
    }

    private void load(int number) throws IOException, URISyntaxException {
        info("Trying load data from TM service " + number + " page");
        if (!fileExist("events", number)) {
            String request = buildRequest(number);
            saveToFile(buildFilePath("events", number), executeRequest(request));
        } else {
            info(this, "File for page # " + number + " exists");
        }
        EventsPage eventsPage = parseFile("events", number);
        processEventPage(eventsPage, number);
    }

    private void processEventPage(EventsPage page, int currentPage) {
        tryable(() -> {
            EventsPage.PageInfo info = page.getPage();
            insertEvents(page.getEmbedded().getEvents());
            if (currentPage < info.getTotalPages()) {
                load(currentPage + 1);
            }
        });
    }

    private void insertEvents(Object[] events) throws UnknownHostException {
        info(this, "Trying save " + events.length + " events to Elasticsearch");
        TransportClient client = createClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for (Object event : events) {
            try {
                LinkedHashMap map = (LinkedHashMap) event;
                String id = (String) map.get("id");
                String eventUrl = (String) map.get("eventUrl");
                Optional<String> content;
                if (count >= 1000) {
                    content = Optional.empty();
                } else {
                    content = getPageContent(eventUrl);
                }
                Optional<EventsPage.Location> location = getLocation(map);
                location.map(l -> ((LinkedHashMap) event).put("location", l));
                content.map(c -> ((LinkedHashMap) event).put("pageContent", c));
                bulkRequest.add(client.prepareIndex(INDEX_NAME, "event")
                        .setSource(JsonHelper.toJson(event, false))
                        .setId(id));
                if (count % 5 == 0) {
                    bulkRequest.get();
                    client.close();
                    client = createClient();
                    bulkRequest = client.prepareBulk();
                    info(this, "Saved : " + count + " events");
                }
                count++;
            } catch (Exception e) {
                error(this, "Can't deserialize " + event);
            }
        }
        BulkResponse bulkResponse = bulkRequest.get();
        if (bulkResponse.hasFailures()) {
            error(this, "Error saving data, " + bulkResponse.buildFailureMessage());

        }
        client.close();
        info(this, "Saved " + events.length + " events to Elasticsearch");
    }

    private Optional<EventsPage.Location> getLocation(LinkedHashMap map) {
        try {
            LinkedHashMap embedded = (LinkedHashMap) map.get("_embedded");
            ArrayList<Object> venue = (ArrayList<Object>) embedded.get("venue");
            if (venue == null || venue.isEmpty() || venue.get(0) == null) {
                return Optional.empty();
            }
            LinkedHashMap<String, Object> firstVenue = (LinkedHashMap<String, Object>) venue.get(0);
            LinkedHashMap<String, Object> location = (LinkedHashMap<String, Object>) firstVenue.get("location");
            if (!location.isEmpty()) {
                EventsPage.Location point = new EventsPage.Location(Double.valueOf((String) location.get("latitude")),
                        Double.valueOf((String) location.get("longitude")));
                System.out.println("LOCATION : " + point);
                return Optional.of(point);
            }
        } catch (Exception e) {
            error(this, e);
        }
        return Optional.empty();
    }

    @Override
    public void enableMapping() {
        tryable(() -> {
            TransportClient client = createClient();
            String mapping = XContentFactory.jsonBuilder().startObject().startObject(EVENT_TYPE).startObject("properties")
                    .startObject("location").field("type", "geo_point").endObject().endObject().string();
            PutMappingResponse putMappingResponse = client.admin().indices().preparePutMapping(INDEX_NAME)
                    .setType(EVENT_TYPE).setSource(mapping).execute().actionGet();
            info(this, "Mapping update result : " + putMappingResponse.isAcknowledged());
        });
    }

    @Override
    public void preload() {
        RestSyncService service = new RestSyncService();
        //  service.removeIndex();
        //  service.createIndex();
        service.enableMapping();
        service.load();
    }

    private void removeIndex() {
        tryable(() -> {
            TransportClient client = createClient();
            DeleteIndexResponse delete = client.admin().indices().delete(new DeleteIndexRequest(INDEX_NAME)).actionGet();
            if (delete.isAcknowledged()) {
                info(this, "Index : " + INDEX_NAME + " was removed");
            } else {
                error(this, " Can't remove Index : " + INDEX_NAME);
            }
        });

    }

    private Optional<String> getPageContent(String eventUrl) {
        try {
            info(this, "Loading content from : " + eventUrl);
            InputStream inputStream = executeRequest(eventUrl);
            if (inputStream == null)
                return Optional.empty();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                return Optional.of(br.lines().collect(Collectors.joining(System.lineSeparator())));
            }
        } catch (Exception e) {
            error(this, "Content unavailable for : " + eventUrl);
            return Optional.empty();
        }
    }

    private IndexResponse insertEvent(TransportClient client, String eventJson, String id) {
        return client.prepareIndex(INDEX_NAME, EVENT_TYPE)
                .setSource(eventJson)
                .setId(id)
                .get();
    }

    private IndexResponse insertSingleEvent(String eventJson, String id) throws UnknownHostException {
        TransportClient client = createClient();

        IndexResponse response = client.prepareIndex(INDEX_NAME, EVENT_TYPE)
                .setSource(eventJson)
                .setId(id)
                .get();
        client.close();
        return response;
    }

    private TransportClient createClient() throws UnknownHostException {
        return TransportClient.builder().build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
    }

    private EventsPage parseFile(String folder, Integer number) throws URISyntaxException, IOException {
        return JsonHelper.getMapper().readValue(new File(buildFilePath(folder, number)), EventsPage.class);
    }

    private String buildFilePath(String folder, Integer pageNumber) {
        return FILES_LOCATION + folder + "\\page-" + pageNumber + ".json";
    }

    public void createIndex() {
        tryable(() -> {
            TransportClient client = createClient();
            CreateIndexResponse createResponse = client.admin().indices().
                    create(Requests.createIndexRequest(INDEX_NAME)).actionGet();
            if (createResponse.isAcknowledged())
                info(this, "Index : " + INDEX_NAME + " was created");
            client.close();
        });
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        //   new RestSyncService().load(1);
//        RestSyncService service = new RestSyncService();
//        service.createIndex();
//        service.enableMapping();
//        System.out.println(events.getPage());
    }
}
