package com.epam.search.processors;

import com.epam.search.common.RequestHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.epam.search.common.LoggingUtil.info;
import static org.jsoup.helper.StringUtil.isBlank;

/**
 * Created by Dmytro_Kovalskyi on 24.02.2016.
 */
public class TMProcessor {
    private static final String BASE_DOMAIN = "http://www.ticketmaster.com";
    private static final String VENUE_BASE_DOMAIN = "http://www.ticketmaster.com/venue/";

    public Set<ArtistInfo> fetchAttractionInfo(ArrayList<Object> attractions) {
        return attractions.stream().map(a -> {
            String name = (String) ((Map<String, Object>) a).get("name");
            String url = (String) ((Map<String, Object>) a).get("url");
            return fetchArtistInfo(name, url);
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    public Set<VenueInfo> fetchVenueInfo(ArrayList<Object> venues) {
        return venues.stream().map(a -> {
            String name = (String) ((Map<String, Object>) a).get("name");
            String id = (String) ((Map<String, Object>) a).get("id");
            return fetchVenueInfo(name, id);
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    private Optional<ArtistInfo> fetchArtistInfo(String name, String subUrl) {
        String url = buildAttractionUrl(subUrl);
        String result = RequestHelper.readResult(RequestHelper.executeRequest(url));
        info(this, "Trying to fetch data from : " + url);
        String biography = parseContent(result);
        if (isBlank(biography)) {
            return Optional.empty();
        } else {
            info(this, "TicketMaster fetched for : " + name + "\t" + biography);
        }
        ArtistInfo info = new ArtistInfo(name, biography);
        return Optional.of(info);
    }

    private Optional<VenueInfo> fetchVenueInfo(String name, String subUrl) {
        String url = buildVenueUrl(subUrl);
        String result = RequestHelper.readResult(RequestHelper.executeRequest(url));
        info(this, "Trying to fetch data from : " + url);
        VenueInfo info = parseVenueContent(name, result);
        return Optional.ofNullable(info);
    }

    private String parseContent(String content) {
        Document document = Jsoup.parse(content);
        String result = "";
        Elements select = document.select("#swap-synopsis p");
        if (select.size() != 0) {
            result = select.toString();
        }
        return result;
    }

    private VenueInfo parseVenueContent(String name, String content) {
        VenueInfo venue = new VenueInfo();
        venue.setName(name);
        Document document = Jsoup.parse(content);
        Elements result = document.select(".artistImage img");
        if (!result.isEmpty()) {
            String image = result.first().attr("src");
            venue.setImage(image);
        }
        result = document.select("#venueDetailDiv .neutral-container");
        if(!result.isEmpty()){
            String details = result.first().html();
            venue.setDetails(details);
        }
        return venue.isEmpty() ? null : venue;
    }

    private String buildAttractionUrl(String subUrl) {
        return BASE_DOMAIN + subUrl;
    }

    private String buildVenueUrl(String subUrl) {
        return VENUE_BASE_DOMAIN + subUrl;
    }

    public static class ArtistInfo {
        private String name;
        private String biography;

        public ArtistInfo() {
        }

        public ArtistInfo(String name, String biography) {
            this.name = name;
            this.biography = biography;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getBiography() {
            return biography;
        }

        public void setBiography(String biography) {
            this.biography = biography;
        }
    }

    public static class VenueInfo {
        private String name;
        private String image;
        private String details;

        public VenueInfo() {
        }

        public VenueInfo(String name, String image, String details) {
            this.name = name;
            this.image = image;
            this.details = details;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getImage() { return image; }

        public void setImage(String image) { this.image = image; }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public boolean isEmpty(){
            return image == null && details == null;
        }
    }
}
