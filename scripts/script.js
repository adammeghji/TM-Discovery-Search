var search_keyword_TM_url = 'https://app.ticketmaster.com/discovery/v1/events.json?apikey=7elxdku9GGG5k8j0Xm8KWdANDgecHMV0'
var search_keyword_EPAM_url = 'http://localhost:8080/rest/discovery/v1/events'
var fuzzy_search_keyword_EPAM_url = 'http://localhost:8080/rest/discovery/v1/events?fuzzy=true'
var extended_search_keyword_EPAM_url = 'http://localhost:8080/rest/discovery/v1/events/fuzzy'
var dsl_search_keyword_EPAM_url = 'http://9a10af202d21cb3e2ac605bb697085e2.us-west-1.aws.found.io:9200/discovery/event/_search'

$("#search_keyword_TM").click(function () {
    var url = search_keyword_TM_url + '&' + prepareGetParams(["#keyword1_TM"]);
    console.log(url);

    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#search_keyword_TM_result').empty();
            if (response._embedded == undefined) {
                $('#search_keyword_TM_result').append("no result");
                return;
            }
            var events = response._embedded.events;

            for (var i = 0; i < events.length; i++) {
                var item = $(eventToListItem(events[i]));
                $('#search_keyword_TM_result').append(item);
                item.click(events[i],
                    function (eventClick) {
                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }
        })

});


$("#fuzzy_search_keyword_TM").click(function () {
    var url = search_keyword_TM_url + '&' + prepareGetParams(["#keyword2_TM"]);
    console.log(url);

    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#fuzzy_search_keyword_TM_result').empty();
            if (response._embedded == undefined) {
                $('#fuzzy_search_keyword_TM_result').append("no result");
                return;
            }
            var events = response._embedded.events;
            for (var i = 0; i < events.length; i++) {

                var item = $(eventToListItem(events[i]));
                $('#fuzzy_search_keyword_TM_result').append(item)
                item.click(events[i],
                    function (eventClick) {
                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }
        })

});


$("#dsl_search_keyword_TM").click(function () {
    var url = search_keyword_TM_url + '&' + $("#keyword3_TM").val();
    console.log(url);

    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#dsl_search_keyword_TM_result').empty();
            if (response._embedded == undefined) {
                $('#dsl_search_keyword_TM_result').append("no result");
                return;
            }
            var events = response._embedded.events;
            for (var i = 0; i < events.length; i++) {

                var item = $(eventToListItem(events[i]));
                $('#dsl_search_keyword_TM_result').append(item)
                item.click(events[i],
                    function (eventClick) {
                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }
        })

});

$("#extended_search_keyword_TM").click(function () {
    var url = search_keyword_TM_url + '&' + $("#keyword4_TM").val();
    console.log(url);

    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#extended_search_keyword_TM_result').empty();
            if (response._embedded == undefined) {
                $('#extended_search_keyword_TM_result').append("no result");
                return;
            }
            var events = response._embedded.events;
            for (var i = 0; i < events.length; i++) {

                var item = $(eventToListItem(events[i]));
                $('#extended_search_keyword_TM_result').append(item)
                item.click(events[i],
                    function (eventClick) {
                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }
        })

});


$("#search_keyword_EPAM").click(function () {
    var url = search_keyword_EPAM_url + '?' + prepareGetParams(["#keyword1_EPAM"]);
    console.log(url);


    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#search_keyword_EPAM_result').empty();
            if (response.length == 0) {
                $('#search_keyword_EPAM_result').append("no result");
                return;
            }
            var events = response.result;
            for (var i = 0; i < events.length; i++) {

                var item = $(searchToListItem(events[i]));

                $('#search_keyword_EPAM_result').append(item);

                item.click(events[i],
                    function (eventClick) {

                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }


        })

});

$("#fuzzy_search_keyword_EPAM").click(function () {
    var url = fuzzy_search_keyword_EPAM_url + '&' + prepareGetParams(["#keyword2_EPAM"]).toLowerCase();
    console.log(url);


    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#fuzzy_search_keyword_EPAM_result').empty();
            if (response.length == 0) {
                $('#fuzzy_search_keyword_EPAM_result').append("no result");
                return;
            }
            var events = response.result;
            for (var i = 0; i < events.length; i++) {

                var item = $(searchToListItem(events[i]));

                $('#fuzzy_search_keyword_EPAM_result').append(item);

                item.click(events[i],
                    function (eventClick) {

                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }


        })

});


$("#dsl_search_keyword_EPAM").click(function () {

    var url = dsl_search_keyword_EPAM_url;
    console.log(url);

    var data = $("#keyword3_EPAM").val();
    console.log(data);
    //data = escape(data);
    //data = JSON.parse(data);
    console.log(data);


    //{ "dsl":"GET _search { "query": {"fuzzy": { "_embedded.categories.name": "music" } }}", "page" : 0, "size" : 5 }

    var json = sendRequest(url, 'POST',
        function (response) {
            console.log(response)
            $('#dsl_search_keyword_EPAM_result').empty();
            if (response.length == 0) {
                $('#dsl_search_keyword_TM_result').append("no result");
                return;
            }
            var events = response.hits.hits;
            for (var i = 0; i < events.length; i++) {

                var item = $(elasticsearchToListItem(events[i]));

                $('#dsl_search_keyword_EPAM_result').append(item);

                item.click(events[i],
                    function (eventClick) {

                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }


        }, data, {"Content-Type": "application/json"})

});


$("#extended_search_keyword_EPAM").click(function () {
    var url = extended_search_keyword_EPAM_url + '?' + prepareGetParams(["#keyword4_EPAM", "#fuzziness4_EPAM", "#boost4_EPAM", "#prefixLength4_EPAM", "#maxExpansions4_EPAM"]).toLowerCase();
    console.log(url);


    var json = sendRequest(url, 'GET',
        function (response) {
            console.log(response)
            $('#extended_search_keyword_EPAM_result').empty();
            if (response.length == 0) {
                $('#extended_search_keyword_EPAM_result').append("no result");
                return;
            }
            var events = response.result;
            for (var i = 0; i < events.length; i++) {

                var item = $(searchToListItem(events[i]));

                $('#extended_search_keyword_EPAM_result').append(item);

                item.click(events[i],
                    function (eventClick) {

                        fillModalFromItem(eventClick.data);
                        $('#myModal').modal({
                            show: 'false'
                        });
                    }
                );
            }


        })

});

var eventToListItem = function (event) {
    var item = '<li class="list-group-item">'
    item = item + event.id + '<br/>';
    item = item + event.name + '<br/>';
    item = item + '</li>'
    return item;
};

var fillModalFromItem = function (event) {
    $('#myModal .modal-body').empty();

    //  $('#myModal .modal-body').append(syntaxHighlight(JSON.stringify(event,null,2)));
    var node = new PrettyJSON.view.Node({
        el: $('#myModal .modal-body'),
        data: event
    });
    node.expandAll();
    $('#myModal .modal-body').append(node);


};

var searchToListItem = function (search) {
    var event = search.source;
    var item = '<li class="list-group-item">'
    item = item + event.id + ' <span style="color:red"><b>' + search.score + '</b></span><br/>';
    item = item + event.name + '<br/>';
    item = item + '</li>'
    return item;
};

var elasticsearchToListItem = function (search) {
    var event = search._source;
    var item = '<li class="list-group-item">'
    item = item + event.id + ' <span style="color:red"><b>' + search._score + '</b></span><br/>';
    item = item + event.name + '<br/>';
    item = item + '</li>'
    return item;
};

var prepareGetParams = function (inputFields) {
    var params = "";
    console.log(inputFields);
    for (i = 0; i < inputFields.length; i++) {
        var val = $(inputFields[i]).val();
        console.log(val);
        if (val != '') {
            params = params + $(inputFields[i]).attr("qname") + "=" + val;
            if (i != inputFields.length - 1) {
                params = params + '&';
            }
        }


    }
    return params;
};


//universal ajax request sender
var sendRequest = function (url, method, callback, json, callHeaders) {

    $.ajax({
        type: method,
        url: url,
        async: true,
        data: json,
        headers: callHeaders,
        success: function (response, textStatus, jqXHR) {

            callback(response);
        },
        error: function (xhr, status, err) {
            console.log(status + " - " + err);
        }
    });
};


function syntaxHighlight(json) {
    if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}
