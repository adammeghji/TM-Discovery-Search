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

    public Set<ArtistInfo> fetchAttractionInfo(ArrayList<Object> attractions) {
        return attractions.stream().map(a -> {
            String name = (String) ((Map<String, Object>) a).get("name");
            String url = (String) ((Map<String, Object>) a).get("url");
            return fetchArtistInfo(name, url);
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

    private String parseContent(String content) {
        Document document = Jsoup.parse(content);
        String result = "";
        Elements select = document.select("#swap-synopsis p");
        if (select.size() != 0) {
            result = select.toString();
        }
        return result;
    }

    private String buildAttractionUrl(String subUrl) {
        return BASE_DOMAIN + subUrl;
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
}
