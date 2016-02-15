package com.epam.search.services.impl;

import com.epam.search.services.PlainSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import static org.elasticsearch.index.query.QueryBuilders.geoDistanceQuery;

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
        } else {
            response = client.prepareSearch(RestSyncService.INDEX_NAME)
                    .setQuery(QueryBuilders.matchPhraseQuery(param, phrase))
                    .setFrom(page * size).setSize(size)
                    .execute()
                    .actionGet();
        }
        client.close();
        return new SearchResult(processResult(response.getHits()), page, size, response.getHits().getTotalHits());
    }

    public void search() {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(geoDistanceQuery("location")
                        .distance(300, DistanceUnit.KILOMETERS)
                        .point(40, -73)
                        .geoDistance(GeoDistance.ARC)).execute().actionGet();
        System.out.println(response);

    }

    public void searchDSL2() {
        String dsl = "GET _search\n" +
                "{ \n" +
                "   \"query\" : {\n" +
                "        \"match_all\" : {\n" +
                "        }\n" +
                "    },\n" +
                "    \"from\" : 0, \n" +
                "    \"size\" : 10\n" +
                "}";
        SearchResult searchResult = searchDSL(dsl, 0, 1000);
        System.out.println(searchResult);

    }

    public static void main(String[] args) {
        SearchServiceImpl service = new SearchServiceImpl();
        service.searchDSL2();
    }
}
