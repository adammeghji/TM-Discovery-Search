package com.epam.search.rest;

import com.epam.search.common.JsonHelper;
import com.epam.search.requests.DslRequest;
import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    Object searchDsl(@RequestBody DslRequest request) {
        return searchService.searchDsl(request.getDsl());
    }

    @RequestMapping(path = "/dates", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult datesSearch(@RequestParam(value = "q", defaultValue = "") String data,
                                           @RequestParam(value = "from", defaultValue = "2015-01-01") String from,
                                           @RequestParam(value = "to", defaultValue = "2016-12-12") String to) {
        return searchService.dateSearch(data, from, to);
    }

    @RequestMapping(path = "/location", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult locationSearch(@RequestParam(value = "lat", defaultValue = "43") Double latitude,
                                              @RequestParam(value = "lon", defaultValue = "43") Double longitude,
                                              @RequestParam(value = "dist", defaultValue = "1000") Integer distance) {
        return searchService.searchNear(latitude, longitude, distance);
    }

    @RequestMapping(path = "/complex", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    SearchService.SearchResult complexSearch(@RequestParam(value = "q", defaultValue = "") String data,
                                             @RequestParam(value = "from", defaultValue = "2015-01-01") String from,
                                             @RequestParam(value = "to", defaultValue = "2016-12-12") String to,
                                             @RequestParam(value = "lat", defaultValue = "43") Double latitude,
                                             @RequestParam(value = "lon", defaultValue = "43") Double longitude,
                                             @RequestParam(value = "dist", defaultValue = "1000") Integer distance) {
        return searchService.complexSearch(data, from, to, latitude, longitude, distance);
    }

    private String prepareResponse(List<SearchService.SingleSearchResult> search, boolean pretty) {
        return JsonHelper.toJson(search, pretty);
    }
}