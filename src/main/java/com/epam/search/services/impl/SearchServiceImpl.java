package com.epam.search.services.impl;

import com.epam.search.services.SearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService {
    @Override
    public List<SearchResult> search(String phrase, int page, int size) {
        return search("_all", phrase, page, size);
    }


    @Override
    public List<SearchResult> search(String phrase, float minScore, int page, int size) {
        throw new UnsupportedOperationException("Method search(String, float, int, int) not supported by SearchServiceImpl");
    }

    @Override
    public List<SearchResult> search(String field, String phrase, float minScore, int page, int size) {
        throw new UnsupportedOperationException("Method search(String, String, float, int, int) not supported by SearchServiceImpl");
    }

    @Override
    public List<SearchResult> search(String param, String phrase, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchQuery(param, phrase))
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return processResult(response.getHits());
    }
}
