package com.epam.search.processors;

import com.epam.search.common.RequestHelper;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Dmytro_Kovalskyi on 23.02.2016.
 */
public class FlickrProcessor implements SearchProcessor {
    public static final int IMAGE_AMOUNT = 5;
    private static final String API_KEY = "aa4e4b72791977121ecfe13f1f40e26d";
    private static final String sharedSecret = "cbb6d2bc9fff35cc";

    public Set<String> fetchImages(String eventName) {
        Set<String> result = new HashSet<>();
        try {
            Flickr f = new Flickr(API_KEY, sharedSecret, new REST());
            SearchParameters parameters = new SearchParameters();
            parameters.setTags(new String[]{eventName});
            PhotoList<Photo> search = f.getPhotosInterface().search(parameters, IMAGE_AMOUNT, 1);
            result.addAll(fetchPhotos(search));
        } catch (Exception e) {
            error(this, e);
        }
        return result;
    }

    private Set<String> fetchPhotos(PhotoList<Photo> photos) {
        Set<String> result;
        result = photos.parallelStream().filter(p -> p.getUrl() != null).map(p -> {
            info(this, "Trying to fetch image from : " + p.getUrl());
            String content = RequestHelper.readResult(RequestHelper.executeRequest(p.getUrl()));
            return fetchImage(content);
        }).collect(Collectors.toSet());
        return result;
    }

    private String fetchImage(String content) {
        Document document = Jsoup.parse(content);
        String image = document.select("meta[property=og:image]").attr("content");
        return image;
    }


    public static void main(String[] args) throws Exception {
        FlickrProcessor processor = new FlickrProcessor();
        System.out.println(processor.fetchImages("American Wrestling"));
    }
}
