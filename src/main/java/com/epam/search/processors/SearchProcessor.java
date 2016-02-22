package com.epam.search.processors;

import java.util.Set;

/**
 * Created by Dmytro_Kovalskyi on 22.02.2016.
 */
public interface SearchProcessor {
    Set<String> fetchImages(String eventName);
}
