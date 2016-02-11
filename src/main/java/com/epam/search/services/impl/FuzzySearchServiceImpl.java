package com.epam.search.services.impl;

import com.epam.search.services.FuzzySearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

/**
 * Created by Dmytro_Kovalskyi on 08.02.2016.
 */
@Service("fuzzySearchService")
public class FuzzySearchServiceImpl implements FuzzySearchService {
    @Override
    public SearchResult search(String phrase, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.fuzzyQuery("_all", phrase))
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()),page, size, response.getHits().getTotalHits());
    }

    @Override
    public SearchResult search(String phrase, float minScore, int page, int size) {
        return search("_all", phrase, minScore, page, size);
    }

    @Override
    public SearchResult search(String field, String phrase, float minScore, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.fuzzyQuery(field, phrase))
                .setMinScore(minScore)
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()),page, size, response.getHits().getTotalHits());
    }

    @Override
    public SearchResult fuzzySearch(String field, String phrase, float boost, int fuzziness, int prefixLength,
                                    int maxExpansions, float minScore, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.fuzzyQuery(field, phrase)
                        .boost(boost)
                        .fuzziness(Fuzziness.fromEdits(fuzziness))
                        .prefixLength(prefixLength)
                        .maxExpansions(maxExpansions))
                .setMinScore(minScore)
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()),page, size, response.getHits().getTotalHits());
    }

}
