# TM-Discovery-Search
How to start

At first run EventsPreloader '''from com.epam.search.*''' It loads events from remote server and stores them in elasticsearch

After that in root directory execute following commands '''mvn clean package tomcat7:run '''. 
It builds war file and deploy it into embedded Tomcat.
And than you can use browser to execute search request throught url '''http://localhost:8080/rest/search?q=query''' 
where '''query''' - string for search.

#Query examples

'''http://localhost:8080/rest/search?q=rockz'''
'''http://localhost:8080/rest/search?q=bananaz'''
