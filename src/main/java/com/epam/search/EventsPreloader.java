package com.epam.search;

import com.epam.search.services.SyncService;
import com.epam.search.services.impl.RestSyncService;
import java.io.IOException;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class EventsPreloader {
    public static void main(String[] args) throws IOException {
        SyncService service = new RestSyncService();
        service.load();
    }
}
