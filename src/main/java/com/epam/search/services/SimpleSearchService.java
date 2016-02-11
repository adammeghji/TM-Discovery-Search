package com.epam.search.services;

/**
 * Created by Dmytro_Kovalskyi on 10.02.2016.
 */
public interface SimpleSearchService {
    SearchService.SearchResult search(String data, boolean fuzzy, boolean fullPhrase, float minScore, int page, int size);

    SearchService.SearchResult searchDsl(String data);

    SearchService.SearchResult fuzzySearch(String data, float boost, int fuzziness,
                                           int prefixLength, int maxExpansions, float minScore, int page, int size);

    SearchService.SearchResult searchDsl(String data, int page, int size);
}
