package com.epam.search.processors;

import java.util.HashSet;
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
        //Set<String> images = googleProcessor.fetchImages(eventName);
        AdditionInfo info = new AdditionInfo();
        info.setGoogleData(google);
        Optional<ContentProcessor.ParseResult> result = contentProcessor.fetchContent(eventUrl);
        result.map(pr -> {
            UniversePage page = new UniversePage();
            page.addImage(pr.getImage());
            page.setDescription(pr.getDescription());
            info.setUniversePage(page);
            return "";
        });

        return info;
    }


    public static class AdditionInfo {
        private GoogleProcessor.GoogleResults.GoogleData googleData;
        private UniversePage universePage;

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
    }
}
