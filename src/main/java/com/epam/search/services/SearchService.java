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

    default List<SingleSearchResult> processResult(SearchHits hits) {
        List<SingleSearchResult> result = new ArrayList<>();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            SingleSearchResult singleResult = new SingleSearchResult();
            singleResult.setId(searchHit.getId());
            singleResult.setScore(searchHit.getScore());
            singleResult.setSource(searchHit.getSource());
            result.add(singleResult);
        }
        return result;
    }


    default SearchService.SearchResult searchDSL(String dsl, int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.simpleQueryStringQuery(dsl))
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()),page, size, response.getHits().getTotalHits());
    }

    default SearchResult getAllEvents(int page, int size) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchAllQuery())
                .setFrom(page * size).setSize(size)
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()),page, size, response.getHits().getTotalHits());
    }


    class SingleSearchResult {
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

    class SearchResult {
        public SearchResult(List<SingleSearchResult> result, int page, int size, long total) {
            this.result = result;
            this.page = page;
            this.size = size;
            this.total = total;
        }

        public SearchResult() {
        }

        private List<SingleSearchResult> result;
        private int page;
        private int size;
        private long total;

        public List<SingleSearchResult> getResult() {
            return result;
        }

        public void setResult(List<SingleSearchResult> result) {
            this.result = result;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getTotal() {
            return total;
        }

        public void setTotal(long total) {
            this.total = total;
        }
    }
}
