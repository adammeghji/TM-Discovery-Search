package com.epam.search.services.impl;

import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
@Service
public class GatewaySearchService implements SimpleSearchService {
    @Autowired
    @Qualifier("fuzzySearchService")
    private SearchService fuzzySearchService;

    @Autowired
    @Qualifier("searchService")
    private SearchService searchService;

    @Override
    public List<SearchService.SearchResult> search(String data, boolean fuzzy, float minScore, int page, int size) {
        if (data == null || data.isEmpty())
            return searchService.getAllEvents(page, size);
        if (fuzzy)
            return processFuzzySearch(data, minScore, page, size);
        else
            return processSearch(data, page, size);
    }

    private List<SearchService.SearchResult> processFuzzySearch(String data, float minScore, int page, int size) {
        if (!data.contains(":"))
            return fuzzySearchService.search(data, minScore, page, size);
        String[] splited = data.split(":");
        String field = splited[0];
        String phrase = splited[1];

        return fuzzySearchService.search(field, phrase, minScore, page, size);
    }

    private List<SearchService.SearchResult> processSearch(String data, int page, int size) {
        if (!data.contains(":"))
            return searchService.search(data, page, size);
        String[] splited = data.split(":");
        String field = splited[0];
        String phrase = splited[1];
        return searchService.search(field, phrase, page, size);

    }

    @Override
    public List<SearchService.SearchResult> fuzzySearch(String data, float boost, int fuzziness,
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
    public List<SearchService.SearchResult> searchDsl(String data, int page, int size) {
        return searchService.searchDSL(data, page, size);
    }

    @Override
    public List<SearchService.SearchResult> searchDsl(String data) {
        return searchDsl(data, 0, 1000);
    }
}
