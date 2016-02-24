package com.epam.search.processors;

import com.epam.search.common.JsonHelper;

import java.util.*;

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

    public Object process(Map<String, Object> event) {
        AdditionInfo original = null;
        if (event.containsKey("info")) {
            info(this, "Event has Info");
            original = JsonHelper.getMapper().convertValue(event.get("info"), AdditionInfo.class);
        } else {
            info(this, "No info");
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
        if (additionalInfo.getUniversePage() != null) {
            info(this, "got TM");
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

        if (original.getGoogleData() == null || original.getGoogleData().isEmpty()) {
            info(this, "call Google");
            GoogleProcessor.GoogleResults.GoogleData google = googleProcessor.fetchLinks(eventName);
            info.setGoogleData(google);
        }
        if (original.flickrImages == null || original.flickrImages.isEmpty()) {
            info(this, "call Flickr");
            Set<String> flickrImages = flickrProcessor.fetchImages(eventName);
            if (!flickrImages.isEmpty()) {
                info.setFlickrImages(flickrImages);
            }
        }
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
        if (original.attractions == null || original.attractions.isEmpty()) {
            info(this, "call TM processor");
            Set<TMProcessor.ArtistInfo> newAttractions = tmProcessor.fetchAttractionInfo(attractions);
            if (!newAttractions.isEmpty()) {
                info.setAttractions(newAttractions);
            }
        }

        if (original.getUniversePage() == null) {
            info(this, "call Content processor");
            Optional<ContentProcessor.ParseResult> result = contentProcessor.fetchContent(eventUrl);
            result.map(pr -> {
                UniversePage page = new UniversePage();
                page.addImage(pr.getImage());
                page.setDescription(pr.getDescription());
                info.setUniversePage(page);
                return "";
            });
        }

        return info;
    }


    public static class AdditionInfo {
        private GoogleProcessor.GoogleResults.GoogleData googleData;
        private UniversePage universePage;
        private Set<String> flickrImages;
        private Set<TMProcessor.ArtistInfo> attractions;
        private Map<String, String> wikiAttractions;
        private Map<String, String> wikiVenues;

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