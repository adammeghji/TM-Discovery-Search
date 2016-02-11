package com.epam.search.services.impl;

import com.epam.search.services.PlainSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
@Service("searchService")
public class SearchServiceImpl implements PlainSearchService {
    @Override
    public SearchResult search(String phrase, boolean fullPhrase, int page, int size) {
        return search("_all", phrase, fullPhrase, page, size);
    }

    @Override
    public SearchResult search(String param, String phrase, boolean fullPhrase, int page, int size) {
        TransportClient client = createClient();

        SearchResponse response;
        if (!fullPhrase) {
            response = client.prepareSearch(RestSyncService.INDEX_NAME)
                    .setQuery(QueryBuilders.matchQuery(param, phrase))
                    .setFrom(page * size).setSize(size)
                    .execute()
                    .actionGet();
            client.close();
        } else {
            response = client.prepareSearch(RestSyncService.INDEX_NAME)
                    .setQuery(QueryBuilders.matchPhraseQuery(param, phrase))
                    .setFrom(page * size).setSize(size)
                    .execute()
                    .actionGet();
            client.close();
        }
        return new SearchResult(processResult(response.getHits()), page, size, response.getHits().getTotalHits());
    }
}
