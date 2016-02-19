package com.epam.search.processors;

import java.util.Set;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class BingProcessor {
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
    public Set<String> fetchImages(String name) {
     //   System.out.println(SEARCH_URL);
        String forSearch = String.format(SEARCH_URL, name);
        System.out.println(forSearch);
        return null;
    }

    public static void main(String[] args) {
        BingProcessor bingProcessor = new BingProcessor();
        Set<String> prague = bingProcessor.fetchImages("Prague");
    }
}
