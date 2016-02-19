package com.epam.search.services.impl;

import com.epam.search.common.JsonHelper;
import com.epam.search.processors.AdditionalInfoProcessor;
import com.epam.search.services.GrabberService;
import com.epam.search.services.SearchService;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epam.search.common.ErrorUtil.tryable;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class GrabberServiceImpl extends ElasticService implements GrabberService {
    private int count = 0;

    private List<SearchService.SingleSearchResult> getAllEvents() throws Exception {
        long eventAmount = getEventCount();
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize((int) eventAmount)
                .execute()
                .actionGet();
        client.close();
        SearchService.SearchResult searchResult =
                new SearchService.SearchResult(processSearchResult(response.getHits()), 0, 0, response.getHits().getTotalHits());
        return searchResult.getResult();
    }

    private long getEventCount() throws Exception {
        TransportClient client = createClient();
        CountResponse response = client.prepareCount(RestSyncService.INDEX_NAME)
                .execute()
                .actionGet();
        client.close();
        return response.getCount();
    }

    private void processEvents(List<SearchService.SingleSearchResult> events) throws Exception {
        for (SearchService.SingleSearchResult singleSearchResult : events) {
            Object event = processEvent(singleSearchResult.getSource());
            insertSingleEvent(JsonHelper.toJson(event, false), singleSearchResult.getId());
            count++;
            info(this, "Grabbed info for " + count + " events");
        }
    }

    public void run() {
        tryable(() -> {
            List<SearchService.SingleSearchResult> events = getAllEvents();
            info(this, "Trying to process " + events.size() + " events");
            processEvents(events);
        });
    }

    private Object processEvent(Map<String, Object> event) {
        AdditionalInfoProcessor processor = new AdditionalInfoProcessor();
        return processor.process(event);
    }

    private List<SearchService.SingleSearchResult> processSearchResult(SearchHits hits) {
        List<SearchService.SingleSearchResult> result = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            SearchService.SingleSearchResult singleResult = new SearchService.SingleSearchResult();
            singleResult.setId(searchHit.getId());
            singleResult.setScore(searchHit.getScore());
            singleResult.setSource(searchHit.getSource());
            result.add(singleResult);
        }
        return result;
    }

    public static void main(String[] args) {
        GrabberServiceImpl grabber = new GrabberServiceImpl();
        grabber.run();
    }
}
