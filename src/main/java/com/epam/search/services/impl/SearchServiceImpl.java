package com.epam.search.services.impl;

import com.epam.search.services.PlainSearchService;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.geo.GeoDistance;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import static org.elasticsearch.index.query.QueryBuilders.geoDistanceQuery;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
@Service("searchService")
public class SearchServiceImpl implements PlainSearchService {

    public static final String DATES_FIELD = "dates.start.localDate";

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

    @Override
    public SearchResult searchNear(double latitude, double longitude, int distance) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(createGeoQuery(latitude, longitude, distance)).execute().actionGet();
        return new SearchResult(processResult(response.getHits()));
    }

    private GeoDistanceQueryBuilder createGeoQuery(double latitude, double longitude, int distance) {
        return geoDistanceQuery("location")
                .distance(distance, DistanceUnit.KILOMETERS)
                .point(latitude, longitude)
                .geoDistance(GeoDistance.ARC);
    }

    //service.dateSearch("EUFF", "2015-10-06", "2016-12-09")
    @Override
    public SearchResult dateSearch(String field, String phrase, String from, String to) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(QueryBuilders.matchQuery(field, phrase))
                .setPostFilter(QueryBuilders.rangeQuery(DATES_FIELD).from(from).to(to))
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()));
    }

    // System.out.println(service.complexSearch("", "2015-10-06", "2016-12-09", 44d, 44d, 40));
    @Override
    public SearchResult complexSearch(String field, String phrase, String from, String to,
                                      double latitude, double longitude, int distance) {
        TransportClient client = createClient();
        SearchResponse response = client.prepareSearch(RestSyncService.INDEX_NAME)
                .setQuery(createGeoQuery(latitude, longitude, distance))
                .setPostFilter(QueryBuilders.rangeQuery(DATES_FIELD).from(from).to(to))
                .setPostFilter(QueryBuilders.matchQuery(field, phrase))
                .execute()
                .actionGet();
        client.close();
        return new SearchResult(processResult(response.getHits()));
    }


    public static void main(String[] args) {
        SearchServiceImpl service = new SearchServiceImpl();
        // System.out.println(service.searchNear(49d, 49d, 100));
         System.out.println(service.dateSearch("name","EUFF", "2015-10-06", "2016-12-09"));
       // System.out.println(service.complexSearch("_all", "", "2015-10-06", "2016-12-09", 44d, 44d, 40));
    }

}
