package com.epam.search.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.UnknownHostException;

/**
 * Created by Dmytro_Kovalskyi on 08.02.2016.
 */
public interface SearchService {

    String search(String phrase) throws UnknownHostException, JsonProcessingException;
}
