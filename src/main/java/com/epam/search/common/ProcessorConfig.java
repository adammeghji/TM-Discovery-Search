package com.epam.search.common;

/**
 * Created by Dmytro_Kovalskyi on 25.02.2016.
 */
public class ProcessorConfig {
    private static boolean googleProcessorEnabler = false;
    private static boolean wikiProcessorEnabler = false;
    private static boolean youtubeProcessorEnabler = true;
    private static boolean instagramProcessorEnabler = false;
    private static boolean foursquareProcessorEnabler = false;
    private static boolean flickrProcessorEnabler = true;
    private static boolean tmProcessorEnabler = true;
    private static boolean contentProcessorEnabler = true;

    public static boolean isGoogleProcessorEnabler() {
        return googleProcessorEnabler;
    }

    public static void setGoogleProcessorEnabler(boolean googleProcessorEnabler) {
        ProcessorConfig.googleProcessorEnabler = googleProcessorEnabler;
    }

    public static boolean isWikiProcessorEnabler() {
        return wikiProcessorEnabler;
    }

    public static void setWikiProcessorEnabler(boolean wikiProcessorEnabler) {
        ProcessorConfig.wikiProcessorEnabler = wikiProcessorEnabler;
    }

    public static boolean isYoutubeProcessorEnabler() {
        return youtubeProcessorEnabler;
    }

    public static void setYoutubeProcessorEnabler(boolean youtubeProcessorEnabler) {
        ProcessorConfig.youtubeProcessorEnabler = youtubeProcessorEnabler;
    }

    public static boolean isInstagramProcessorEnabler() {
        return instagramProcessorEnabler;
    }

    public static void setInstagramProcessorEnabler(boolean instagramProcessorEnabler) {
        ProcessorConfig.instagramProcessorEnabler = instagramProcessorEnabler;
    }

    public static boolean isFoursquareProcessorEnabler() {
        return foursquareProcessorEnabler;
    }

    public static void setFoursquareProcessorEnabler(boolean foursquareProcessorEnabler) {
        ProcessorConfig.foursquareProcessorEnabler = foursquareProcessorEnabler;
    }

    public static boolean isFlickrProcessorEnabler() {
        return flickrProcessorEnabler;
    }

    public static void setFlickrProcessorEnabler(boolean flickrProcessorEnabler) {
        ProcessorConfig.flickrProcessorEnabler = flickrProcessorEnabler;
    }

    public static boolean isTmProcessorEnabler() {
        return tmProcessorEnabler;
    }

    public static void setTmProcessorEnabler(boolean tmProcessorEnabler) {
        ProcessorConfig.tmProcessorEnabler = tmProcessorEnabler;
    }

    public static boolean isContentProcessorEnabler() {
        return contentProcessorEnabler;
    }

    public static void setContentProcessorEnabler(boolean contentProcessorEnabler) {
        ProcessorConfig.contentProcessorEnabler = contentProcessorEnabler;
    }
}
