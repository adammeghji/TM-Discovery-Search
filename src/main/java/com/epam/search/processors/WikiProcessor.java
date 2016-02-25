package com.epam.search.processors;

import net.sourceforge.jwbf.core.contentRep.Article;
import net.sourceforge.jwbf.mediawiki.actions.editing.GetRevision;
import net.sourceforge.jwbf.mediawiki.bots.MediaWikiBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.epam.search.common.LoggingUtil.info;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by Dmytro_Kovalskyi on 24.02.2016.
 */
public class WikiProcessor {
    public String fetchInfo(String query) {
        String clearQuery = cleanQuery(query);
        System.out.println(clearQuery);
        MediaWikiBot wikiBot = new MediaWikiBot("https://en.wikipedia.org/w/");
        Article article = wikiBot.getArticle(clearQuery, GetRevision.CONTENT);
        String result = article.getText();
        info(this, "Fetched from WIKI for : " + query + " >>> " + result);
        return result;
    }

    public Map<String, String> fetchAttractionInfo(ArrayList<Object> attractions) {
        return fetchStandardData(attractions);
    }

    public Map<String, String> fetchVenuesInfo(ArrayList<Object> venues) {
        return fetchStandardData(venues);
    }

    private Map<String, String> fetchStandardData(ArrayList<Object> attractions) {
        Map<String, String> result = new HashMap<>();
        attractions.forEach(a -> {
            String name = (String) ((Map<String, Object>) a).get("name");
            String info = fetchInfo(name);
            if (isBlank(info))
                info = "NO CONTENT";
            result.put((String) ((Map<String, Object>) a).get("id"), info);
        });
        return result;
    }

    private String cleanQuery(String query) {
        return query.replaceAll("tickets", "").replaceAll("Tickets", "").trim();
    }

    public static void main(String[] args) {
        WikiProcessor processor = new WikiProcessor();
        processor.fetchInfo("David Ramirez Tickets");
    }
}
