package com.epam.search.processors;

import com.epam.search.common.JsonHelper;
import com.epam.search.common.RequestHelper;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class GoogleProcessor implements SearchProcessor {
    private static String address = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
    private static String charset = "UTF-8";

    public GoogleResults.GoogleData fetchLinks(String eventName) {
        GoogleResults result = new GoogleResults();
        try {
            URL url = new URL(address + URLEncoder.encode(eventName, charset));
            result = JsonHelper.getMapper().readValue(readData(url.openStream()), GoogleResults.class);
            info(this, "Fetched info from google : " + result);
        } catch (Exception e) {
            error(this, e);
        }
        return result.getResponseData();
    }

    private String readData(InputStream stream) {
        return RequestHelper.readResult(stream);
    }

    @Override
    public Set<String> fetchImages(String eventName) {
        String query = "https://www.google.com.ua/search?q=prague&safe=off&hs=MrB&source=lnms&tbm=isch";
        Set<String> result = new HashSet<>();
        try {
            RequestHelper.saveToFile("D:\\google.html", RequestHelper.executeRequest(query));

        } catch (Exception e) {
            error(this, e);
        }
        info(this, "Fetched images from google : " + result);
        return result;
    }

    public static void main(String[] args) {
        GoogleProcessor processor = new GoogleProcessor();
        processor.fetchImages("");
    }

    public static class GoogleResults {

        private GoogleData responseData;

        public GoogleData getResponseData() {
            return responseData;
        }

        public void setResponseData(GoogleData responseData) {
            this.responseData = responseData;
        }

        public String toString() {
            return "GoogleData[" + responseData + "]";
        }


        static class GoogleData {
            private List<Result> results;

            public List<Result> getResults() {
                return results;
            }

            public void setResults(List<Result> results) {
                this.results = results;
            }

            public String toString() {
                return "Results[" + results + "]";
            }

            public boolean isEmpty() {
                return results == null || results.isEmpty();
            }

        }

        static class Result {
            private String url;
            private String title;

            public String getUrl() {
                return url;
            }

            public String getTitle() {
                return title;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String toString() {
                return "Result[url:" + url + ",title:" + title + "]";
            }
        }

    }

}
