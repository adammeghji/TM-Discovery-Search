package com.epam.search.processors;

import com.epam.search.common.JsonHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.epam.search.common.LoggingUtil.info;
import static org.apache.http.util.TextUtils.isBlank;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class AdditionalInfoProcessor {
    private GoogleProcessor googleProcessor = new GoogleProcessor();
    private ContentProcessor contentProcessor = new ContentProcessor();
    private SearchProcessor flickrProcessor = new FlickrProcessor();

    public Object process(Map<String, Object> event) {
        String eventName = (String) event.get("name");
        String eventUrl = (String) event.get("eventUrl");
        AdditionInfo additionalInfo = getAdditionalInfo(eventName, eventUrl);
        AdditionInfo merged = merge(event, additionalInfo);
        info(this, "MERGE RESULT : " + merged);
        event.put("info", merged);
        return event;
    }

    private AdditionInfo merge(Map<String, Object> event, AdditionInfo additionalInfo) {
        if (!event.containsKey("info"))
            return additionalInfo;
        AdditionInfo merged = JsonHelper.getMapper().convertValue(event.get("info"), AdditionInfo.class);
        if (!additionalInfo.getGoogleData().isEmpty()) {
            merged.setGoogleData(additionalInfo.googleData);
        }
        if (additionalInfo.flickrImages != null && !additionalInfo.flickrImages.isEmpty()) {
            merged.setFlickrImages(additionalInfo.flickrImages);
        }
        if (additionalInfo.getUniversePage() != null) {
            merged.setUniversePage(additionalInfo.getUniversePage());
        }
        return merged;
    }

    private AdditionInfo getAdditionalInfo(String eventName, String eventUrl) {
        AdditionInfo info = new AdditionInfo();

        //GoogleProcessor.GoogleResults.GoogleData google = googleProcessor.fetchLinks(eventName);
        //info.setGoogleData(google);

        Set<String> flickrImages = flickrProcessor.fetchImages(eventName);
        if (!flickrImages.isEmpty()) {
            info.setFlickrImages(flickrImages);
        }

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
        private Set<String> flickrImages;

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
