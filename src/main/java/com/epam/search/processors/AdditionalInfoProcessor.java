package com.epam.search.processors;

import com.epam.search.common.JsonHelper;
import com.epam.search.common.ProcessorConfig;

import java.util.*;
import java.lang.*;

import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;
import static org.apache.http.util.TextUtils.isBlank;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class AdditionalInfoProcessor {
    private GoogleProcessor googleProcessor = new GoogleProcessor();
    private ContentProcessor contentProcessor = new ContentProcessor();
    private SearchProcessor flickrProcessor = new FlickrProcessor();
    private TMProcessor tmProcessor = new TMProcessor();
    private WikiProcessor wikiProcessor = new WikiProcessor();
    private YouTubeProcessor youTubeProcessor = new YouTubeProcessor();

    public Object process(Map<String, Object> event) {
        AdditionInfo original = null;
        info(this, "START " + event.get("id").toString());
        if (event.containsKey("info")) {
            info(this, "!!!! Event has Info");
            original = JsonHelper.getMapper().convertValue(event.get("info"), AdditionInfo.class);
        } else {
            info(this, "!!!! No info");
            original = new AdditionInfo();
        }

        AdditionInfo additionalInfo = getAdditionalInfo(event, original);
        AdditionInfo merged = merge(original, additionalInfo);
        info(this, "MERGE RESULT : " + merged);
        event.put("info", merged);
        return event;
    }

    private AdditionInfo merge(AdditionInfo original, AdditionInfo additionalInfo) {

        AdditionInfo merged = original;
        if (additionalInfo.getGoogleData() != null && !additionalInfo.getGoogleData().isEmpty()) {
            info(this, "got Google");
            merged.setGoogleData(additionalInfo.googleData);
        }
        if (additionalInfo.flickrImages != null && !additionalInfo.flickrImages.isEmpty()) {
            info(this, "got Flickr");
            merged.setFlickrImages(additionalInfo.flickrImages);
        }

        if (additionalInfo.wikiAttractions != null && !additionalInfo.wikiAttractions.isEmpty()) {
            info(this, "got WIKI Attraction processor");
            merged.setWikiAttractions(additionalInfo.getWikiAttractions());
        }

        if (additionalInfo.wikiVenues != null && !additionalInfo.wikiVenues.isEmpty()) {
            info(this, "got WIKI Venues processor");
            merged.setWikiVenues(additionalInfo.getWikiVenues());
        }

        if (additionalInfo.attractions != null && !additionalInfo.attractions.isEmpty()) {
            info(this, "got TM attr processor");
            merged.setAttractions(additionalInfo.getAttractions());
        }

        if (additionalInfo.venues != null && !additionalInfo.venues.isEmpty()) {
            info(this, "got TM venue processor");
            merged.setVenues(additionalInfo.getVenues());
        }

        if (additionalInfo.videos != null && !additionalInfo.videos.isEmpty()) {
            info(this, "got You Tube processor");
            merged.setVideos(additionalInfo.getVideos());
        }

        if (additionalInfo.getUniversePage() != null) {
            info(this, "got Content processor");
            merged.setUniversePage(additionalInfo.getUniversePage());
        }
        return merged;
    }


    private AdditionInfo getAdditionalInfo(Map<String, Object> event, AdditionInfo original) {
        String eventName = (String) event.get("name");
        String eventUrl = (String) event.get("eventUrl");
        ArrayList<Object> attractions = (ArrayList<Object>) ((Map) event.get("_embedded")).get("attractions");
        ArrayList<Object> venues = (ArrayList<Object>) ((Map) event.get("_embedded")).get("venue");

        AdditionInfo info = new AdditionInfo();
        if (ProcessorConfig.isGoogleProcessorEnabler()) {
            processGoogle(original, eventName, info);
        }
        if (ProcessorConfig.isFlickrProcessorEnabler()) {
            processFlickr(original, eventName, info);
        }
        if (ProcessorConfig.isWikiProcessorEnabler()) {
            processWiki(original, attractions, venues, info);
        }
        if (ProcessorConfig.isTmProcessorEnabler()) {
            processTM(original, attractions, venues, info);
        }
        if (ProcessorConfig.isContentProcessorEnabler()) {
            processContent(original, eventUrl, info);
        }
        if (ProcessorConfig.isYoutubeProcessorEnabler()) {
            processYouTube(original, attractions, info);
        }
        return info;
    }

    private void processYouTube(AdditionInfo original, ArrayList<Object> attractions, AdditionInfo info) {
        if (original.videos == null || original.videos.isEmpty()) {
            try {
                info(this, "call You Tube processor");
                Set<YouTubeProcessor.YouTubeInfo> newVideos = youTubeProcessor.fetchArtistInfo(attractions);
                if (!newVideos.isEmpty()) {
                    info.setVideos(newVideos);
                }
            } catch (Exception e) {
                error(this, e);
            }
        }
    }

    private void processContent(AdditionInfo original, String eventUrl, AdditionInfo info) {
        if (original.getUniversePage() == null && eventUrl != null) {
            try {
                info(this, "call Content processor");
                Optional<ContentProcessor.ParseResult> result = contentProcessor.fetchContent(eventUrl);
                result.map(pr -> {
                    UniversePage page = new UniversePage();
                    page.addImage(pr.getImage());
                    page.setDescription(pr.getDescription());
                    info.setUniversePage(page);
                    return "";
                });
            } catch (Exception e) {
                error(this, e);
            }
        }
    }

    private void processTM(AdditionInfo original, ArrayList<Object> attractions, ArrayList<Object> venues, AdditionInfo info) {
        if (original.attractions == null || original.attractions.isEmpty()) {
            try {
                info(this, "call TM attr processor");
                Set<TMProcessor.ArtistInfo> newAttractions = tmProcessor.fetchAttractionInfo(attractions);
                if (!newAttractions.isEmpty()) {
                    info.setAttractions(newAttractions);
                }
            } catch (Exception e) {
                error(this, e);
            }
        }

        if (original.venues == null || original.venues.isEmpty()) {
            try {
                info(this, "call TM venue processor");
                Set<TMProcessor.VenueInfo> newVenues = tmProcessor.fetchVenueInfo(venues);
                if (!newVenues.isEmpty()) {
                    info.setVenues(newVenues);
                }
            } catch (Exception e) {
                error(this, e);
            }
        }
    }

    private void processWiki(AdditionInfo original, ArrayList<Object> attractions, ArrayList<Object> venues, AdditionInfo info) {
        if (original.wikiAttractions == null || original.wikiAttractions.isEmpty()) {
            info(this, "call WIKI Attraction processor");
            Map<String, String> newWikiAttractions = wikiProcessor.fetchAttractionInfo(attractions);
            if (!newWikiAttractions.isEmpty()) {
                info.setWikiAttractions(newWikiAttractions);
            }
        }


        if (original.wikiVenues == null || original.wikiVenues.isEmpty()) {
            info(this, "call WIKI Venues processor");
            Map<String, String> newWikiVenues = wikiProcessor.fetchVenuesInfo(venues);
            if (!newWikiVenues.isEmpty()) {
                info.setWikiVenues(newWikiVenues);
            }
        }
    }

    private void processFlickr(AdditionInfo original, String eventName, AdditionInfo info) {
        if (original.flickrImages == null || original.flickrImages.isEmpty()) {
            try {
                info(this, "call Flickr");
                Set<String> flickrImages = flickrProcessor.fetchImages(eventName);
                if (!flickrImages.isEmpty()) {
                    info.setFlickrImages(flickrImages);
                } else {
                    flickrImages = flickrProcessor.fetchImages(eventName.split(" ")[0]);
                    if (!flickrImages.isEmpty()) {
                        info.setFlickrImages(flickrImages);
                    } else {
                        flickrImages = flickrProcessor.fetchImages(eventName.split(" ")[1]);
                        if (!flickrImages.isEmpty()) {
                            info.setFlickrImages(flickrImages);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    private void processGoogle(AdditionInfo original, String eventName, AdditionInfo info) {
        if (original.getGoogleData() == null || original.getGoogleData().isEmpty()) {
            info(this, "call Google");
            GoogleProcessor.GoogleResults.GoogleData google = googleProcessor.fetchLinks(eventName);
            info.setGoogleData(google);
        }
    }


    public static class AdditionInfo {
        private GoogleProcessor.GoogleResults.GoogleData googleData;
        private UniversePage universePage;
        private Set<String> flickrImages;
        private Set<TMProcessor.ArtistInfo> attractions;
        private Set<TMProcessor.VenueInfo> venues;
        private Map<String, String> wikiAttractions;
        private Map<String, String> wikiVenues;
        private Set<YouTubeProcessor.YouTubeInfo> videos;

        public AdditionInfo() {
        }

        public AdditionInfo(GoogleProcessor.GoogleResults.GoogleData googleData, UniversePage universePage) {
            this.googleData = googleData;
            this.universePage = universePage;
        }

        public GoogleProcessor.GoogleResults.GoogleData getGoogleData() {
            return googleData;
        }

        public void setGoogleData(GoogleProcessor.GoogleResults.GoogleData googleData) {
            this.googleData = googleData;
        }

        public UniversePage getUniversePage() {
            return universePage;
        }

        public void setUniversePage(UniversePage universePage) {
            this.universePage = universePage;
        }

        public Set<String> getFlickrImages() {
            return flickrImages;
        }

        public void setFlickrImages(Set<String> flickrImages) {
            this.flickrImages = flickrImages;
        }

        public Set<TMProcessor.ArtistInfo> getAttractions() {
            return attractions;
        }

        public void setAttractions(Set<TMProcessor.ArtistInfo> attractions) {
            this.attractions = attractions;
        }

        public Set<TMProcessor.VenueInfo> getVenues() {
            return venues;
        }

        public void setVenues(Set<TMProcessor.VenueInfo> venues) {
            this.venues = venues;
        }

        public Map<String, String> getWikiAttractions() {
            return wikiAttractions;
        }

        public void setWikiAttractions(Map<String, String> wikiAttractions) {
            this.wikiAttractions = wikiAttractions;
        }

        public Map<String, String> getWikiVenues() {
            return wikiVenues;
        }

        public void setWikiVenues(Map<String, String> wikiVenues) {
            this.wikiVenues = wikiVenues;
        }

        public Set<YouTubeProcessor.YouTubeInfo> getVideos() {
            return videos;
        }

        public void setVideos(Set<YouTubeProcessor.YouTubeInfo> videos) {
            this.videos = videos;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("AdditionInfo{");
            sb.append("googleData=").append(googleData);
            sb.append(", universePage=").append(universePage);
            sb.append(", flickrImages=").append(flickrImages);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class UniversePage {
        private Set<String> images = new HashSet<>();
        private String description;

        public UniversePage() {
        }

        public UniversePage(Set<String> images, String description) {
            this.images = images;
            this.description = description;
        }

        private void addImage(String image) {
            if (!isBlank(image)) {
                images.add(image);
            }
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {

            this.description = description;

        }

        public Set<String> getImages() {
            return images;
        }

        public void setImages(Set<String> images) {
            this.images = images;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("UniversePage{");
            sb.append("images=").append(images);
            sb.append(", description='").append(description).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
