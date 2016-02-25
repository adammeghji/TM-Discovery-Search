package com.epam.search.services.impl;

import com.epam.search.common.JsonHelper;
import com.epam.search.processors.AdditionalInfoProcessor;
import com.epam.search.services.GrabberService;
import com.epam.search.services.SearchService;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epam.search.common.ErrorUtil.tryable;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class GrabberServiceImpl extends ElasticService implements GrabberService {
    private volatile int count = 0;
    private volatile int errors = 0;


    private List<SearchService.SingleSearchResult> getAllEvents(int from, int to) throws Exception {
        if (to < from)
            throw new IllegalArgumentException("to param should be greater than from param");

        List<SearchService.SingleSearchResult> results = new ArrayList<>();
        TransportClient client = createClient();
        int size = to - from;
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(size)
                .setFrom(from)
                .execute()
                .actionGet();
        results.addAll(processSearchResult(response.getHits()));

        client.close();
        return results;
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
        events.stream().parallel().forEach(singleSearchResult -> {
            try {
                Object event = processEvent(singleSearchResult.getSource());
                insertSingleEvent(JsonHelper.toJson(event, false), singleSearchResult.getId());
            } catch (Exception e) {
                errors++;
                info(this, "ERRORS " + errors);
            }
            count++;
            info(this, "Grabbed info for " + count + " events");
        });
    }

    private Object processEvent(Map<String, Object> event) {
        AdditionalInfoProcessor processor = new AdditionalInfoProcessor();
        return processor.process(event);
    }

    @Override
    public void grab(int from, int to) {
        tryable(() -> {
            List<SearchService.SingleSearchResult> events = getAllEvents(from, to);
            info(this, "======");
            info(this, "====");
            info(this, "==");
            info(this, ".");
            info(this, "Trying to process " + events.size() + " events from # " + from + " to #" + to);
            processEvents(events);
        });
    }

    @Override
    public void grab(int from) {
        boolean completed = false;
        int fromNumber = from;
        while (!completed) {
            grab(fromNumber, fromNumber + 100);
            fromNumber += 100;
            if (fromNumber >= 70000)
                completed = true;
        }
    }

    @Override
    public void grab(String phrase) {
        tryable(() -> {
            info(this, "Trying grab for : " + phrase);
            List<SearchService.SingleSearchResult> searchResult = search(phrase);
            info(this, "Found : " + searchResult.size() + " events");
            processEvents(searchResult);
        });
    }
}
