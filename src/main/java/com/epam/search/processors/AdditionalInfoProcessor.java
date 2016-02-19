package com.epam.search.processors;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.apache.http.util.TextUtils.isBlank;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class AdditionalInfoProcessor {

    public Object process(Map<String, Object> event) {
        String eventName = (String) event.get("name");
        String eventUrl = (String) event.get("eventUrl");
        AdditionInfo additionalInfo = getAdditionalInfo(eventName, eventUrl);

        event.put("info", additionalInfo);
        return event;
    }

    private AdditionInfo getAdditionalInfo(String eventName, String eventUrl) {
        GoogleProcessor googleProcessor = new GoogleProcessor();
        ContentProcessor contentProcessor = new ContentProcessor();
        GoogleProcessor.GoogleResults.GoogleData google = googleProcessor.fetchLinks(eventName);
        Set<String> images = googleProcessor.fetchImages(eventName);
        AdditionInfo info = new AdditionInfo(google, images);
        Optional<ContentProcessor.ParseResult> result = contentProcessor.fetchContent(eventUrl);
        result.map(pr -> {
            info.addImage(pr.getImage());
            info.setDescription(pr.getDescription());
            return "";
        });
        return info;
    }


    public static class AdditionInfo {
        private GoogleProcessor.GoogleResults.GoogleData googleData;
        private Set<String> images;
        private String description;

        public AdditionInfo() {
        }

        public AdditionInfo(GoogleProcessor.GoogleResults.GoogleData googleData, Set<String> images) {
            this.googleData = googleData;
            this.images = images;
        }

        public GoogleProcessor.GoogleResults.GoogleData getGoogleData() {
            return googleData;
        }

        public void setGoogleData(GoogleProcessor.GoogleResults.GoogleData googleData) {
            this.googleData = googleData;
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
    }
}
