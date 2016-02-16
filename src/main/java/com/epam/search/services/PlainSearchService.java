package com.epam.search.services;

/**
 * Created by Dmytro_Kovalskyi on 11.02.2016.
 */
public interface PlainSearchService extends SearchService {
    SearchResult search(String phrase, boolean fullPhrase, int page, int size);

    SearchResult search(String param, String phrase, boolean fullPhrase, int page, int size);

    SearchResult searchNear(double latitude, double longitude, int distance);

    //service.dateSearch("EUFF", "2015-10-06", "2016-12-09")
    SearchResult dateSearch(String field, String phrase, String from, String to);

    // System.out.println(service.complexSearch("", "2015-10-06", "2016-12-09", 44d, 44d, 40));
    SearchResult complexSearch(String field, String phrase, String from, String to,
                               double latitude, double longitude, int distance);
}
