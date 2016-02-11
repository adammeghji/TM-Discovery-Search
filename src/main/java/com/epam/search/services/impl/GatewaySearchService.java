package com.epam.search.services.impl;

import com.epam.search.services.FuzzySearchService;
import com.epam.search.services.PlainSearchService;
import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public SearchService.SearchResult searchDsl(String data, int page, int size) {
        return searchService.searchDSL(data, page, size);
    }

    @Override
    public SearchService.SearchResult searchDsl(String data) {
        return searchDsl(data, 0, 1000);
    }
}
