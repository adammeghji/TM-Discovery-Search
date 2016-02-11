package com.epam.search.rest;

import com.epam.search.common.JsonHelper;
import com.epam.search.requests.DslRequest;
import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.epam.search.common.LoggingUtil.error;

@RestController
@RequestMapping("/discovery/v1/events")
public class EventResource {

    @Autowired
    private SimpleSearchService searchService;


    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult search(@RequestParam(value = "q", defaultValue = "") String data,
                                      @RequestParam(value = "fuzzy", defaultValue = "false") boolean fuzzy,
                                      @RequestParam(value = "minScore", defaultValue = "0") float minScore,
                                      @RequestParam(value = "fullPhrase", defaultValue = "false") boolean fullPhrase,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "1000") int size) {
        return searchService.search(data, fuzzy, fullPhrase, minScore, page, size);
    }

    @RequestMapping(path = "/fuzzy", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult fuzzySearch(@RequestParam(value = "q", defaultValue = "") String data,
                                           @RequestParam(value = "boost", defaultValue = "1.0") float boost,
                                           @RequestParam(value = "fuzziness", defaultValue = "2") int fuzziness,
                                           @RequestParam(value = "prefixLength", defaultValue = "0") int prefixLength,
                                           @RequestParam(value = "maxExpansions", defaultValue = "50") int maxExpansions,
                                           @RequestParam(value = "minScore", defaultValue = "0") float minScore,
                                           @RequestParam(value = "page", defaultValue = "0") int page,
                                           @RequestParam(value = "size", defaultValue = "1000") int size) {
        return searchService.fuzzySearch(data, boost, fuzziness, prefixLength, maxExpansions, minScore, page, size);
    }

    @RequestMapping(path = "/dsl", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult searchDsl(@RequestBody DslRequest request) {
        return searchService.searchDsl(request.getDsl(), request.getPage(), request.getSize());
    }

    private String prepareResponse(List<SearchService.SingleSearchResult> search, boolean pretty) {
        try {
            return JsonHelper.toJson(search, pretty);
        } catch (JsonProcessingException e) {
            error(this, e);
            return "Error of processing, " + e.getMessage();
        }
    }
}