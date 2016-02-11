package com.epam.search.services;

import java.util.List;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
public interface SimpleSearchService {
    List<SearchService.SearchResult> search(String data, boolean fuzzy, float minScore, int page, int size);

    List<SearchService.SearchResult> searchDsl(String data);

    List<SearchService.SearchResult> searchDsl(String data, int page, int size);
}
