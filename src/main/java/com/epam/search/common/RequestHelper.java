package com.epam.search.common;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.*;
import java.nio.file.*;
import java.util.stream.Collectors;

import static com.epam.search.common.LoggingUtil.error;

/**
 * Created by Dmytro_Kovalskyi on 17.02.2016.
 */
public class RequestHelper {
    public static InputStream executeRequest(String url) {
        try {
            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 10000);
            HttpClient client = new DefaultHttpClient(httpParams);
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            return response.getEntity().getContent();
        } catch (Exception e) {
            error(null, e);
        }
        return null;
    }

    public static String readResult(InputStream inputStream) {
        try {
            if (inputStream == null)
                return "";
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                return br.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (Exception e) {
            error(RequestHelper.class, "Content unavailable for : " + inputStream);
            return "";
        }
    }

    public static void saveToFile(String fileName, String data) {
        try {
            Path targetPath = new File(fileName).toPath();
            if (!Files.exists(targetPath.getParent())) {
                Files.createDirectories(targetPath.getParent());
            }
            Files.write(targetPath, data.getBytes(), StandardOpenOption.CREATE);
        } catch (Exception e) {
            error(RequestHelper.class, e);
        }
    }

    public static void saveToFile(String fileName, InputStream stream) throws IOException {
        Path targetPath = new File(fileName).toPath();
        if (!Files.exists(targetPath.getParent())) {
            Files.createDirectories(targetPath.getParent());
        }
        Files.copy(stream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        stream.close();
    }
}
