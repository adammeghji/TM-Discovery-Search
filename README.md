# TM-Discovery-Search
How to start

At first run EventsPreloader ```from com.epam.search.*``` It loads events from remote server and stores them in elasticsearch

After that in root directory execute following commands ```mvn clean package tomcat7:run ```. 
It builds war file and deploy it into embedded Tomcat.
And than you can use browser to execute search request throught url ```http://localhost:8080/rest/discovery/v1/events``` 

#Params
q - string for search can be used as ```filed:query``` for example name:music (or muzic when enabled fuzzy search)
fuzzy - boolean value to enable fuzzy search
page - int value to find specific page
size - int value to set amount of elements
minScore - float value to filter by score when used fuzzy search


#Example 1

http://localhost:8080/rest/discovery/v1/events?q=name:muzic&fuzzy=true&page=1&size=5

#Example 2

http://localhost:8080/rest/discovery/v1/events?q=bananaz&fuzzy=true

#DSL support
to execute custom query send POST request into ```http://localhost:8080/rest/discovery/v1/events/dsl``` with ```application/json``` type and use url

#Example3
{
    "dsl":"GET _search {  \"query\": {    \"fuzzy\": {      \"_embedded.categories.name\": \"music\"    }  }}",
    "page" : 0,
    "size" : 5
}

