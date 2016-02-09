package com.epam.search.services.impl;

import com.epam.search.common.JsonHelper;
import com.epam.search.services.SearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.stereotype.Service;

/**
 * Created by Dmytro_Kovalskyi on 08.02.2016.
 */
@Service
public class FuzzySearchService implements SearchService {
    @Override
    public String search(String phrase) throws UnknownHostException, JsonProcessingException {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
            .setQuery(QueryBuilders.fuzzyQuery("name", phrase))
            .setFrom(0).setSize(5)
            .execute()
            .actionGet();
        return JsonHelper.toJson(processResult(response.getHits()));
    }

    private List<SearchResult> processResult(SearchHits hits) {
        List<SearchResult> result = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for(SearchHit searchHit : searchHits) {
            SearchResult singleResult = new SearchResult();
            singleResult.setId(searchHit.getId());
            singleResult.setScore(searchHit.getScore());
            singleResult.setSource(searchHit.getSource());
            result.add(singleResult);
        }
        return result;
    }

    private TransportClient createClient() throws UnknownHostException {
        return TransportClient.builder().build()
            .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
    }

    public static void main(String[] args) throws UnknownHostException, JsonProcessingException {
        FuzzySearchService service = new FuzzySearchService();
        System.out.println(service.search("phrase"));
    }

    public static class SearchResult {
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
