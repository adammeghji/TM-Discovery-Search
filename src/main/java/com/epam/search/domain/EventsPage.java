package com.epam.search.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;

/**
 * Created by Dmytro_Kovalskyi on 09.02.2016.
 */
public class EventsPage {
    @JsonProperty("_links")
    private Object links;
    @JsonProperty("_embedded")
    private Embedded embedded;
    private PageInfo page;

    public Object getLinks() {
        return links;
    }

    public void setLinks(Object links) {
        this.links = links;
    }

    public Embedded getEmbedded() {
        return embedded;
    }

    public void setEmbedded(Embedded embedded) {
        this.embedded = embedded;
    }

    public PageInfo getPage() {
        return page;
    }

    public void setPage(PageInfo page) {
        this.page = page;
    }

    public static class PageInfo {
        private Integer size;
        private Integer totalElements;
        private Integer totalPages;
        private Integer number;

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public Integer getTotalElements() {
            return totalElements;
        }

        public void setTotalElements(Integer totalElements) {
            this.totalElements = totalElements;
        }

        public Integer getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(Integer totalPages) {
            this.totalPages = totalPages;
        }

        public Integer getNumber() {
            return number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("PageInfo{");
            sb.append("size=").append(size);
            sb.append(", totalElements=").append(totalElements);
            sb.append(", totalPages=").append(totalPages);
            sb.append(", number=").append(number);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class Embedded {
        private Object[] events;

        public Object[] getEvents() {
            return events;
        }

        public void setEvents(Object[] events) {
            this.events = events;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Embedded{");
            sb.append("events=").append(Arrays.toString(events));
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("EventsPage{");
        sb.append("links=").append(links);
        sb.append(", embedded=").append(embedded);
        sb.append(", page=").append(page);
        sb.append('}');
        return sb.toString();
    }
}
