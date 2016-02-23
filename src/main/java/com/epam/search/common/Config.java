package com.epam.search.common;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public interface Config {
    //String ELASTIC_HOST = "localhost";
    String ELASTIC_HOST = "9a10af202d21cb3e2ac605bb697085e2.us-west-1.aws.found.io";

    String INDEX_NAME = "discovery";
    String EVENT_TYPE = "event";

    //int ELASTIC_TRANSPORT_PORT = 9300;
    int ELASTIC_TRANSPORT_PORT = 9343;
}
