package com.epam.search.processors;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Set;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class BingProcessor implements SearchProcessor{
    private static String APP_ID = "/HNLqUoeItY7U0frf5iORHgqh1OL4tbDtdrP/k+cvZk";

//    static {
//        tryable(() -> {
//            APP_ID = URLEncoder.encode("/HNLqUoeItY7U0frf5iORHgqh1OL4tbDtdrP/k+cvZk", "UTF-8");
//        });
//    }

    private static int IMAGE_AMOUNT = 3;
    private static String SEARCH_URL = "http://api.search.live.net/json.aspx?AppId=" + APP_ID +
            "&Sources=image&Query=%s&Image.Count=" + IMAGE_AMOUNT +
            "&Image.Offset=0&Image.Filters=Size:Medium";

    // Limit 7 queries per second
    @Override
    public Set<String> fetchImages(String name) {
     //   System.out.println(SEARCH_URL);

        String forSearch = String.format(SEARCH_URL, name);
        System.out.println(forSearch);
        return null;
    }

    public static void getBing() throws Exception {

        HttpClient httpclient = new DefaultHttpClient();

        try {
            HttpGet httpget = new HttpGet("https://api.datamarket.azure.com/Data.ashx/Bing/Search/Web?Query=%27Datamarket%27&$top=10&$format=Json");
            httpget.setHeader("Authorization", "Basic <Your Account Key(Remember add colon character at before the key, then use Base 64 encode it>");

            System.out.println("executing request " + httpget.getURI());

            // Create a response handler
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            System.out.println("----------------------------------------");

        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static void main(String[] args) {
        BingProcessor bingProcessor = new BingProcessor();
        Set<String> prague = bingProcessor.fetchImages("Prague");
    }
}
