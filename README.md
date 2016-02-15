# TM-Discovery-Search
How to start

At first run EventsPreloader ```from com.epam.search.*``` It loads events from remote server and stores them in elasticsearch

After that in root directory execute following commands ```mvn clean package tomcat7:run ```. 
It builds war file and deploy it into embedded Tomcat.
And than you can use browser to execute search request throught url ```http://localhost:8080/rest/discovery/v1/events``` 

##Params
1. q - string for search can be used as ```filed:query``` for example name:music (or muzic when enabled fuzzy search)
2. fuzzy - boolean value to enable fuzzy search, ```default false``` 
3. page - int value to find specific page, ```default 0 ```
4. size - int value to set amount of elements, ```default 1000```
5. minScore - float value to filter by score when used fuzzy search, ```default 0```
6. fullPhrase - use strict search ```default false``` 


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

#Extended Fuzzy search
To change params of fuzzy search use ```http://localhost:8080/rest/discovery/v1/events/fuzzy``` url for GET method

## Allowed params
1. q - string for search can be used as ```filed:query``` for example name:music (or muzic when enabled fuzzy search)
2. fuzziness - int value, the maximum edit distance, ```default 2```, allowed values ```0, 1, 2``` 
3. boost - float value ```default 1.0```
4. prefixLength - int value, the number of initial characters which will not be “fuzzified”. This helps to reduce the number of terms     which must be examined, ```default 0```
5. maxExpansions - int value, the maximum number of terms that the fuzzy query will expand to,  ```default 50```
6. page - int value to find specific page, ```default 0 ```
7. size - int value to set amount of elements, ```default 1000```
8. minScore - float value to filter by score when used fuzzy search, ```default 0```

For more description see [a https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html] (https://www.elastic.co/guide/en/elasticsearch/reference/current/query-dsl-fuzzy-query.html)

## Query example 
http://localhost:8080/rest/discovery/v1/events/fuzzy?q=bananaz&fuzziness=1&prefixLength=2&maxExpansions=1000&boost=1.0
