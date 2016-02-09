package com.epam.search.common;

import static com.epam.search.common.LoggingUtil.error;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class ErrorUtil {
    public static void tryable(Executable executable) {
        try {
            executable.execute();
        } catch(Exception e) {
            error(ErrorUtil.class, e);
        }
    }
}
