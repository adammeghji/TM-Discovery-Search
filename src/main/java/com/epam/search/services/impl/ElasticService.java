package com.epam.search.services.impl;

import com.epam.search.common.Config;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.shield.ShieldPlugin;

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

    protected void insertSingleEvent(String eventJson, String id) {
        TransportClient client = createClient();
        info(this, "Inserts : " + eventJson);
        info(this, "FINISH " + id);
        IndexResponse response = client.prepareIndex(Config.INDEX_NAME, Config.EVENT_TYPE)
                .setSource(eventJson)
                .setId(id)
                .get();
        client.close();
    }

    protected TransportClient createClient() {
        try {
            // Build the settings for our client.
            String clusterId = "9a10af202d21cb3e2ac605bb697085e2"; // Your cluster ID here
            String region = "us-west-1"; // Your region here
            boolean enableSsl = true;

            Settings settings = Settings.settingsBuilder()
                    .put("transport.ping_schedule", "5s")
                    //.put("transport.sniff", false) // Disabled by default and *must* be disabled.
                    .put("cluster.name", clusterId)
                    .put("action.bulk.compress", false)
                    .put("shield.transport.ssl", enableSsl)
                    .put("request.headers.X-Found-Cluster", clusterId)
                    .put("shield.user", "admin:pa55w0rd") // your shield username and password
                    .build();

            String hostname = clusterId + "." + region + ".aws.found.io";
// Instantiate a TransportClient and add the cluster to the list of addresses to connect to.
// Only port 9343 (SSL-encrypted) is currently supported.
            TransportClient client = TransportClient.builder()
                    .addPlugin(ShieldPlugin.class)
                    .settings(settings)
                    .build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), 9343));
            return client;
        } catch (UnknownHostException e) {
            error(this, e);
            return null;
        }
    }
/*
    protected TransportClient createClient() {
        try {
            return TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Config.ELASTIC_HOST), Config.ELASTIC_TRANSPORT_PORT));
        } catch (UnknownHostException e) {
            error(this, e);
            return null;
        }
    }
    */
}
