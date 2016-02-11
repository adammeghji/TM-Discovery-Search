package com.epam.search.services;

/**
 * Created by Dmytro_Kovalskyi on 11.02.2016.
 */
public interface FuzzySearchService extends SearchService {
    SearchResult search(String phrase, int page, int size);

    SearchResult search(String phrase, float minScore, int page, int size);

    SearchResult search(String field, String phrase, float minScore, int page, int size);

    SearchResult fuzzySearch(String field, String phrase, float boost, int fuzziness, int prefixLength,
                             int maxExpansions, float minScore, int page, int size);
}
