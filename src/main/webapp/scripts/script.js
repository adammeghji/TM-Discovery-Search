

var search_keyword_TM_url = 'https://app.ticketmaster.com/discovery/v1/events.json?apikey=7elxdku9GGG5k8j0Xm8KWdANDgecHMV0'
var search_keyword_EPAM_url = 'http://localhost:8080/rest/discovery/v1/events'
var fuzzy_search_keyword_EPAM_url = 'http://localhost:8080/rest/discovery/v1/events?fuzzy=true'

$( "#search_keyword_TM" ).click(function() {
  var url = search_keyword_TM_url + '&' + prepareGetParams(["#keyword1_TM"]);
  console.log(url);

  var json =  sendRequest(url, 'GET',
      function(response){
          console.log(response)
          $('#search_keyword_TM_result').empty();
          var events = response._embedded.events;
          for (var i = 0; i < events.length; i++) {
            var item = $(eventToListItem(events[i]));
            $('#search_keyword_TM_result').append(item)
            item.click(events[i],
                function(eventClick) {
                    fillModalFromItem(eventClick.data);
                    $('#myModal').modal({
                        show: 'false'
                    });
                }
            );
          }
      })

});


$( "#fuzzy_search_keyword_TM" ).click(function() {
  var url = search_keyword_TM_url + '&' + prepareGetParams(["#keyword2_TM"]);
  console.log(url);

  var json =  sendRequest(url, 'GET',
      function(response){
          console.log(response)
          $('#fuzzy_search_keyword_TM_result').empty();
          var events = response._embedded.events;
          for (var i = 0; i < events.length; i++) {

            var item = $(eventToListItem(events[i]));
            $('#fuzzy_search_keyword_TM_result').append(item)
            item.click(events[i],
                function(eventClick) {
                    fillModalFromItem(eventClick.data);
                    $('#myModal').modal({
                        show: 'false'
                    });
                }
            );
          }
      })

});


$( "#search_keyword_EPAM" ).click(function() {
  var url = search_keyword_EPAM_url + '?' + prepareGetParams(["#keyword1_EPAM"]);
  console.log(url);



  var json =  sendRequest(url, 'GET',
      function(response){
          console.log(response)
          $('#search_keyword_EPAM_result').empty();
          var events = response;
          for (var i = 0; i < events.length; i++) {

            var item = $(searchToListItem(events[i]));

            $('#search_keyword_EPAM_result').append(item);

            item.click(events[i],
                function(eventClick) {

                    fillModalFromItem(eventClick.data);
                    $('#myModal').modal({
                        show: 'false'
                    });
                }
            );
          }




      })

});

$( "#fuzzy_search_keyword_EPAM" ).click(function() {
  var url = fuzzy_search_keyword_EPAM_url + '&' + prepareGetParams(["#keyword2_EPAM"]).toLowerCase();
  console.log(url);



  var json =  sendRequest(url, 'GET',
      function(response){
          console.log(response)
          $('#fuzzy_search_keyword_EPAM_result').empty();
          var events = response;
          for (var i = 0; i < events.length; i++) {

            var item = $(searchToListItem(events[i]));

            $('#fuzzy_search_keyword_EPAM_result').append(item);

            item.click(events[i],
                function(eventClick) {

                    fillModalFromItem(eventClick.data);
                    $('#myModal').modal({
                        show: 'false'
                    });
                }
            );
          }




      })

});

var eventToListItem = function(event) {
    var item = '<li class="list-group-item">'
    item = item + event.id + '<br/>';
    item = item + event.name + '<br/>';
    item = item + '</li>'
    return item;
};

var fillModalFromItem = function(event) {
    $('#myModal .modal-body').empty();



  //  $('#myModal .modal-body').append(syntaxHighlight(JSON.stringify(event,null,2)));
    var node = new PrettyJSON.view.Node({
      el:$('#myModal .modal-body'),
      data:event
    });
    node.expandAll();
    $('#myModal .modal-body').append(node);


}

var searchToListItem = function(search) {
    var event = search.source;
    var item = '<li class="list-group-item">'
    item = item + event.id + ' <span style="color:red"><b>' + search.score + '</b></span><br/>';
    item = item + event.name + '<br/>';
    item = item + '</li>'
    return item;
};

var prepareGetParams = function(inputFields) {
    var params="";
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
var sendRequest = function(url, method, callback){

    $.ajax({
        type: method,
        url: url,
        async: true,
        success: function(response, textStatus, jqXHR) {

            callback(response);
        },
        error: function(xhr, status, err) {
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