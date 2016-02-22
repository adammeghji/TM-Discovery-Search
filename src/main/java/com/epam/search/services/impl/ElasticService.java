package com.epam.search.services.impl;

import com.epam.search.common.Config;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public abstract class ElasticService {
    protected IndexResponse insertEvent(TransportClient client, String eventJson, String id) {
        return client.prepareIndex(Config.INDEX_NAME, Config.EVENT_TYPE)
                .setSource(eventJson)
                .setId(id)
                .get();
    }

    protected IndexResponse insertSingleEvent(String eventJson, String id) {
        TransportClient client = createClient();
        info(this, "Inserts : " + eventJson);
        IndexResponse response = client.prepareIndex(Config.INDEX_NAME, Config.EVENT_TYPE)
                .setSource(eventJson)
                .setId(id)
                .get();
        client.close();
        return response;
    }

    protected TransportClient createClient() {
        try {
            return TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.ELASTIC_HOST), Config.ELASTIC_TRANSPORT_PORT));
        } catch (UnknownHostException e) {
            error(this, e);
            return null;
        }
    }
}
