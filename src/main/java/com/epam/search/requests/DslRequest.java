package com.epam.search.requests;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Dmytro_Kovalskyi on 11.02.2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DslRequest {
    private String dsl = "";
    private Integer page = 0;
    private Integer size = 1000;

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
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

}
