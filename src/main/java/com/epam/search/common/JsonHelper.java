package com.epam.search.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static com.epam.search.common.LoggingUtil.error;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class JsonHelper {
    private static ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

    public static String toJson(Object data, boolean pretty) {
        try {

            if (pretty) {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            } else
                return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            error(JsonHelper.class, e);
            return "JSON convert exception";
        }
    }

    public static ObjectMapper getMapper() {
        return mapper;
    }
}
