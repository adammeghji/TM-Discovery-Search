package com.epam.search.processors;

import com.epam.search.common.RequestHelper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.stream.Collectors;

import static com.epam.search.common.LoggingUtil.info;

/**
 * Created by Oleksii_Dziuniak on 2/25/2016.
 */
public class YouTubeProcessor {

    private static final String BASE_DOMAIN = "https://www.youtube.com/results?search_query=" ;

    public Set<YouTubeInfo> fetchArtistInfo(ArrayList<Object> attractions) {
        return attractions.stream().map(a -> {
            String name = (String) ((Map<String, Object>) a).get("name");
            return fetchArtistInfo(name);
        }).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
    }

    private Optional<YouTubeInfo> fetchArtistInfo(String name) {
        String url = buildYouTubeUrl(name);
        String result = RequestHelper.readResult(RequestHelper.executeRequest(url));
        info(this, "Trying to fetch data from : " + url);
        YouTubeInfo info = parseContent(name, result);
        return Optional.ofNullable(info);
    }

    private String buildYouTubeUrl(String name) {
        String subUrl = name.replaceAll(",|show|Show|Tickets|tickets|Event|event","").replace(" ","+");
        return BASE_DOMAIN + subUrl;
    }

    private YouTubeInfo parseContent(String name, String content) {
        Document document = Jsoup.parse(content);
        Elements result = document.select("#results .yt-lockup");
        if(result.isEmpty()){
            return null;
        }
        final YouTubeInfo info = new YouTubeInfo(name);
        result.forEach(e -> {
            String videoId = e.attr("data-context-item-id");
            info.addId(videoId);
        });
        return info;
    }

    public static class YouTubeInfo {
        private String name;
        private Set<String> ids;

        public YouTubeInfo() {
        }

        public YouTubeInfo(String name) {
            this.name = name;
            this.ids = new HashSet<>();
        }

        public Set<String> getIds() {
            return ids;
        }

        public void addId(String id) {
            if(!id.isEmpty()){
                this.ids.add(id);
            }
        }
    }
    
}
