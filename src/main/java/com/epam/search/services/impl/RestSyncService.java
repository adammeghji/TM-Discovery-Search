package com.epam.search.services.impl;

import com.epam.search.domain.EventsPage;
import com.epam.search.services.SyncService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.istack.internal.NotNull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import static com.epam.search.common.ErrorUtil.tryable;
import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class RestSyncService implements SyncService {
    private static final String API_KEY = "bjDPa4wVqDyS1xXY6ASc2S4DGSxpmTNd";
    private static final String FILES_LOCATION = "D:\\search\\";
    private ObjectMapper mapper = new ObjectMapper();

    @NotNull
    private String buildRequest(Integer number) {
        return "https://app.ticketmaster.com/discovery/v1/events.json?"
            + "apikey=" + API_KEY + "&size=1000" + "&number=" + number;
    }

    private InputStream executeRequest(String url) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        } catch(Exception e) {
            error(null, e);
        }
        return null;
    }

    private void saveToFile(String fileName, InputStream stream) throws IOException {
        Path targetPath = new File(fileName).toPath();
        if(!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        stream.close();
    }

    public void load() {
        tryable(() -> {
            load(0);
        });
    }

    private boolean fileExist(String folder, int pageNumber) {
        return new File(buildFilePath(folder, pageNumber)).exists();
    }

    private void load(int number) throws IOException, URISyntaxException {
        info("Trying load data from TM service " + number + " page");
        if(!fileExist("events", number)) {
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
            if(currentPage < info.getTotalPages()) {
                load(currentPage + 1);
            }
        });
    }

    private void insertEvents(Object[] events) throws UnknownHostException {
        info(this, "Trying save " + events.length + " events to Elasticsearch");
        TransportClient client = createClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        for(Object event : events) {
            try {
                LinkedHashMap map = (LinkedHashMap) event;
                String id = (String) map.get("id");
                bulkRequest.add(client.prepareIndex("discovery", "event")
                    .setSource(toJson(event)));
                  //  .setId(id));
            } catch(Exception e) {
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

    private String toJson(Object event) throws JsonProcessingException {
        return mapper.writeValueAsString(event);
    }

    private IndexResponse insertEvent(TransportClient client, String eventJson, String id) {
        return client.prepareIndex("discovery", "event")
            .setSource(eventJson)
            .setId(id)
            .get();
    }

    private IndexResponse insertSingleEvent(String eventJson, String id) throws UnknownHostException {
        TransportClient client = createClient();

        IndexResponse response = client.prepareIndex("discovery", "event")
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
        return mapper.readValue(new File(buildFilePath(folder, number)), EventsPage.class);
    }

    private String buildFilePath(String folder, Integer pageNumber) {
        return FILES_LOCATION + folder + "\\page-" + pageNumber + ".json";
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        EventsPage events = new RestSyncService().parseFile("events", 0);
        System.out.println(events.getPage());
    }
}
