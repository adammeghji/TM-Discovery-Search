package com.epam.search.services;

import com.epam.search.services.impl.RestSyncService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.epam.search.common.LoggingUtil.error;

/**
 * Created by Dmytro_Kovalskyi on 08.02.2016.
 */
public interface SearchService {

    String ELASTIC_HOST = "localhost";
    int ELASTIC_PORT = 9300;

    default TransportClient createClient() {
        try {
            return TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ELASTIC_HOST), ELASTIC_PORT));
        } catch (UnknownHostException e) {
            error(this, e);
            return null;
        }
    }

    default List<SearchResult> processResult(SearchHits hits) {
        List<SearchResult> result = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            SearchResult singleResult = new SearchResult();
            singleResult.setId(searchHit.getId());
            singleResult.setScore(searchHit.getScore());
            singleResult.setSource(searchHit.getSource());
            result.add(singleResult);
        }
        return result;
    }

    default List<SearchResult> searchDSL(String dsl, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.simpleQueryStringQuery(dsl))
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return processResult(response.getHits());
    }

    default List<SearchResult> getAllEvents(int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return processResult(response.getHits());
    }

    List<SearchResult> search(String phrase, float minScore, int page, int size);

    List<SearchResult> search(String field, String phrase, float minScore, int page, int size);

    List<SearchResult> fuzzySearch(String field, String phrase, float boost, int fuzziness, int prefixLength,
                                   int maxExpansions, float minScore, int page, int size);

    List<SearchResult> search(String field, String phrase, int page, int size);

    List<SearchResult> search(String data, int page, int size);


    class SearchResult {
        private String id;
        private Float score;
        private Map<String, Object> source;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public Map<String, Object> getSource() {
            return source;
        }

        public void setSource(Map<String, Object> source) {
            this.source = source;
        }
    }
}
