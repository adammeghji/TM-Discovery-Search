package com.epam.search.services;

/**
 * Created by Dmytro_Kovalskyi on 11.02.2016.
 */
public interface PlainSearchService extends SearchService {
    SearchResult search(String phrase, boolean fullPhrase, int page, int size);

    SearchResult search(String param, String phrase, boolean fullPhrase, int page, int size);
}
