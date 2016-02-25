package com.epam.search.processors;

import java.util.Map;

/**
 * Created by Dmytro_Kovalskyi on 25.02.2016.
 */
public interface Processor {
    ProcessingResult search(Map<String, Object> event);

    interface ProcessingResult {
    }
}
