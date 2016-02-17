package com.epam.search.services.impl;

import com.epam.search.common.JsonHelper;
import com.epam.search.services.FuzzySearchService;
import com.epam.search.services.PlainSearchService;
import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.LinkedHashMap;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
@Service
public class GatewaySearchService implements SimpleSearchService {
    @Autowired
    private FuzzySearchService fuzzySearchService;

    @Autowired
    private PlainSearchService searchService;

    @Override
    public SearchService.SearchResult search(String data, boolean fuzzy, boolean fullPhrase, float minScore, int page, int size) {
        if (data == null || data.isEmpty())
            return searchService.getAllEvents(page, size);
        if (fuzzy)
            return processFuzzySearch(data, minScore, page, size);
        else
            return processSearch(data, fullPhrase, page, size);
    }

    private SearchService.SearchResult processFuzzySearch(String data, float minScore, int page, int size) {
        if (!data.contains(":"))
            return fuzzySearchService.search(data, minScore, page, size);
        String[] splited = data.split(":");
        String field = splited[0];
        String phrase = splited[1];

        return fuzzySearchService.search(field, phrase, minScore, page, size);
    }

    private SearchService.SearchResult processSearch(String data, boolean fullPhrase, int page, int size) {
        if (!data.contains(":"))
            return searchService.search(data, fullPhrase, page, size);
        String[] splited = data.split(":");
        String field = splited[0];
        String phrase = splited[1];
        return searchService.search(field, phrase, fullPhrase, page, size);

    }

    private Pair<String, String> parse(String searchData) {
        String[] splited = searchData.split(":");
        String field = splited[0];
        String phrase = splited[1];
        return new Pair<>(field, phrase);
    }

    @Override
    public SearchService.SearchResult fuzzySearch(String data, float boost, int fuzziness,
                                                  int prefixLength, int maxExpansions, float minScore, int page, int size) {
        String field, phrase;
        if (!data.contains(":")) {
            field = "_all";
            phrase = data;
        } else {
            String[] splited = data.split(":");
            field = splited[0];
            phrase = splited[1];
        }
        return fuzzySearchService.fuzzySearch(field, phrase, boost, fuzziness, prefixLength, maxExpansions, minScore, page, size);
    }

    private SearchService.SearchResult splittable(String data, Executable executable) {
        String field, phrase;
        if (!data.contains(":")) {
            field = "_all";
            phrase = data;
        } else {
            String[] splited = data.split(":");
            field = splited[0];
            phrase = splited[1];
        }
        return executable.execute(field, phrase);
    }

    @Override
    public SearchService.SearchResult searchNear(double latitude, double longitude, int distance) {
        return searchService.searchNear(latitude, longitude, distance);
    }

    @Override
    public SearchService.SearchResult dateSearch(String data, String from, String to) {
        return splittable(data, (field, phrase) -> searchService.dateSearch(field, phrase, from, to));
    }

    @Override
    public SearchService.SearchResult complexSearch(String data, String from, String to,
                                                    double latitude, double longitude, int distance) {
        return splittable(data, (field, phrase) ->
                searchService.complexSearch(field, phrase, from, to, latitude, longitude, distance));
    }

    @Override
    public Object searchDsl(String data) {
        try {
            LinkedHashMap hashMap = (LinkedHashMap) JsonHelper.getMapper().readValue(searchService.searchDSL(data), Object.class);
            hashMap.remove("pageContent");
            return hashMap;
        } catch (IOException e) {
            return new Object();
        }
    }

    @FunctionalInterface
    private interface Executable {
        SearchService.SearchResult execute(String field, String phrase);
    }

    public static void main(String[] args) {
//        GatewaySearchService service = new GatewaySearchService();
//        System.out.println(service.dateSearch("EUFF", "2015-10-06", "2016-12-09"));
    }
}
