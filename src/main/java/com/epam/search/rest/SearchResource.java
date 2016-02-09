package com.epam.search.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchResource {

    private Logger logger = LoggerFactory.getLogger(getClass());


    @RequestMapping(value = "/{data}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public String search(@PathVariable("data") String data) {
        System.out.println("Provider has received request to search: " + data);
        return "Find data" + data;
    }


}