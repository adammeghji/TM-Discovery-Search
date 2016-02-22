package com.epam.search.processors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.epam.search.common.LoggingUtil.error;
import static com.epam.search.common.LoggingUtil.info;
import static com.epam.search.common.RequestHelper.executeRequest;

/**
 * Created by Dmytro_Kovalskyi on 19.02.2016.
 */
public class ContentProcessor {

    public static final String UNIVERSE_SITE = "www.universe.com/events";
    public static final String TICKETWEB_SITE = "www.ticketweb.com";

    public Optional<ParseResult> fetchContent(String url) {
        return getPageContent(url).flatMap(c -> parseContent(c, url));
    }

    private Optional<ParseResult> parseContent(String content, String url) {
        Optional<ParseResult> result = Optional.empty();
        try {
            if (url.contains(UNIVERSE_SITE)) {
                result = parseUniverse(content, result);
            } else if (url.contains(TICKETWEB_SITE)) {
                result = parseTicketWeb(content, result);
            } else {
                info(this, "Can't parse url that doesn't contains " + UNIVERSE_SITE + "\n URL : " + url);
                return Optional.empty();
            }
        } catch (Exception e) {
            error(this, e);
        }
        return result;
    }

    private Optional<ParseResult> parseTicketWeb(String content, Optional<ParseResult> result) {
        Document document = Jsoup.parse(content);
        String description = document.select("meta[property=og:description]").attr("content");

        String image = document.select("meta[property=og:image]").attr("content");
        result = Optional.of(new ParseResult(description, image));
        return result;
    }

    private Optional<ParseResult> parseUniverse(String content, Optional<ParseResult> result) {
        Document document = Jsoup.parse(content);
        String description = document.select("meta[property=og:description]").attr("content");

        String image = document.select("meta[name=twitter:image]").attr("content");
        result = Optional.of(new ParseResult(description, image));
        return result;
    }

    private Optional<String> getPageContent(String eventUrl) {
        try {
            info(this, "Loading content from : " + eventUrl);
            InputStream inputStream = executeRequest(eventUrl);
            if (inputStream == null)
                return Optional.empty();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                return Optional.of(br.lines().collect(Collectors.joining(System.lineSeparator())));
            }
        } catch (Exception e) {
            error(this, "Content unavailable for : " + eventUrl);
            return Optional.empty();
        }
    }

    public static class ParseResult {
        private String description;
        private String image;

        public ParseResult(String description, String image) {
            this.description = description;
            this.image = image;
        }

        public ParseResult() {
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("ParseResult{");
            sb.append("description='").append(description).append('\'');
            sb.append(", image='").append(image).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }
}
