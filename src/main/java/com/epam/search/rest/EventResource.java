package com.epam.search.rest;

import com.epam.search.common.JsonHelper;
import com.epam.search.services.SearchService;
import com.epam.search.services.SimpleSearchService;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    List<SearchService.SearchResult> search(@RequestParam(value = "q", defaultValue = "") String data,
                                            @RequestParam(value = "fuzzy", defaultValue = "false") boolean fuzzy,
                                            @RequestParam(value = "minScore", defaultValue = "0") float minScore,
                                            @RequestParam(value = "page", defaultValue = "0") int page,
                                            @RequestParam(value = "size", defaultValue = "1000") int size,
                                            @RequestParam(value = "pretty", defaultValue = "false") boolean pretty) {

        return searchService.search(data, fuzzy, minScore, page, size);
    }

    @RequestMapping(path = "/dsl", method = RequestMethod.POST, consumes = "application/json" )
    @ResponseStatus(HttpStatus.OK)
    @CrossOrigin(origins = "*")
    public
    @ResponseBody
    List<SearchService.SearchResult> searchDsl(@RequestBody DslRequest request) {
        return searchService.searchDsl(request.getDsl(), request.getPage(), request.getSize());
    }

    private String prepareResponse(List<SearchService.SearchResult> search, boolean pretty) {
        try {
            return JsonHelper.toJson(search, pretty);
        } catch (JsonProcessingException e) {
            error(this, e);
            return "Error of processing, " + e.getMessage();
        }
    }

    class DslRequest {
        @JsonProperty(defaultValue = "")
        private String dsl;
        @JsonProperty(defaultValue = "0")
        private Integer page;
        @JsonProperty(defaultValue = "1000")
        private Integer size;
        private Boolean pretty;

        public String getDsl() {
            return dsl;
        }

        public void setDsl(String dsl) {
            this.dsl = dsl;
        }

        public Integer getPage() {
            return page;
        }

        public void setPage(Integer page) {
            this.page = page;
        }

        public Integer getSize() {
            if (size == null)
                return 1000;
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Boolean getPretty() {
            if (pretty == null)
                return false;
            return pretty;
        }

        public void setPretty(Boolean pretty) {
            this.pretty = pretty;
        }
    }

}