package com.epam.search.common;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public interface Config {
    String ELASTIC_HOST = "localhost";
    String INDEX_NAME = "discovery";
    String EVENT_TYPE = "event";

    int ELASTIC_TRANSPORT_PORT = 9300;
}
