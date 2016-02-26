(function(){

    Object.byString = function(o, s) { // prototype function to return sub object from object by string path
        s = s.replace(/\[(\w+)\]/g, '.$1'); // convert indexes to properties
        s = s.replace(/^\./, ''); // strip a leading dot
        var a = s.split('.');
        for (var i = 0, n = a.length; i < n; ++i) {
            var k = a[i];
            if (k in o) {
                o = o[k];
            } else {
                return;
            }
        }
        return o;
    };

    var search_keyword_TM_url = 'https://app.ticketmaster.com/discovery/v1/events.json?apikey=7elxdku9GGG5k8j0Xm8KWdANDgecHMV0', // base URL for TM
        search_keyword_EPAM_url = 'http://9a10af202d21cb3e2ac605bb697085e2.us-west-1.aws.found.io:9200/discovery/event/_search', // base URL for EPAM
        spinner;

    $(document).ready(function(){

        spinner = $('#spinner');

        // run query on search button click
        $('#search-btn').on('click', function(e){
            e.preventDefault();
            var approach = getApproach();
            if (approach === "TM")
                runTMRequest(); // run TM request
            else
                runEPAMRequest(); // run EPAM request
        });

        // run query on enter
        $('#text-to-search').on('keyup', function(e){
            var input = $(e.target);
            if (e.keyCode == 13){
                input.blur();
                $('#search-btn').trigger('click');
            }
        });

        // get approach: 'TM' or 'EPAM' or 'FUZZY'
        function getApproach(){
            var selected = $("input[type='radio'][name='approach']:checked");
            if (selected.length > 0) return selected.val();
            else return false;
        }

        $("input[type='radio'][name='approach']").on('change', function(){
            var responseContainer = $('#response'),
                responseDetailContainer = $('#response-detail');
            if (getApproach() === "TM") {
                responseContainer.removeClass("col-xs-6").addClass("col-xs-12"); //show right column
                responseDetailContainer.hide(); //hide right column

                $('.typeahead').bind('typeahead:select', function(ev, suggestion) {
                    console.log('Selection: ' + suggestion);
                });

                runTMRequest(); // run TM request
            }else{
                responseContainer.addClass("col-xs-6").removeClass("col-xs-12"); //show right column
                responseDetailContainer.show(); //show right column
                runEPAMRequest(); // run EPAM request
                autoCompleteRunner();
            }
        });

        // runs TM request
        var runTMRequest = function(){
            var keyword = getKeywordValue(),
                url = search_keyword_TM_url + (keyword ? ('&keyword=' + keyword) : '');
/*
            var eventToListItem = function(event) {
                var item = '<li class="list-group-item">'
                item = item + event.id + '<br/>';
                item = item + event.name + '<br/>';
                item = item + '</li>'
                return item;
            };

            //universal ajax request sender
            var sendRequestTM = function(url, method, callback, json, callHeaders){
                $.ajax({
                    type: method,
                    url: url,
                    async: true,
                    data: json,
                    headers: callHeaders,
                    success: function(response, textStatus, jqXHR) {

                        callback(response);
                    },
                    error: function(xhr, status, err) {
                        console.log(status + " - " + err);
                    }
                });
            };

            sendRequestTM(url, 'GET',
                function(response){
                    console.log(response)
                    $('#response').empty();
                    if (response._embedded == undefined) {
                        $('#response').append("no result");
                        return;
                    }
                    var events = response._embedded.events;

                    for (var i = 0; i < events.length; i++) {
                        var item = $(eventToListItem(events[i]));
                        $('#response').append(item);
                        item.click(events[i],
                            function(eventClick) {
                                console.log(eventClick.data);
                                fillModalFromItem(eventClick.data);
                                $('#myModal').modal({
                                    show: 'false'
                                });
                            }
                        );
                    }
                });
*/
            sendRequest(url, 'GET', null, function(json){
                new Column(json, '_embedded.events', url, false);
            });
        };

        // runs EPAM request
        var runEPAMRequest = function(from){
            var keyword = getKeywordValue(),
                url = search_keyword_EPAM_url,
                splitted = keyword.split(":"),
                EPAM_data_match = {},
                EPAM_data = {};

            if (splitted.length === 1){
                EPAM_data_match = {
                    "name": {
                        "query": splitted[0],
                        "operator": "or",
                        "fuzziness": 2,
                        "analyzer" : "my_synonyms"
                    }
                }
            }
            else if (splitted.length > 1){
                if (splitted[0]==="_all") {
                    EPAM_data_match[splitted[0]] = {
                    "query": splitted[1],
                    "operator": "and",
                    "fuzziness": 2,
                    "analyzer" : "my_synonyms"
                };
                } else {
                EPAM_data_match[splitted[0]] = {
                    "query": splitted[1],
                    "operator": "or",
                    "fuzziness": 2,
                    "analyzer" : "my_synonyms"
                };
                }
            }
            EPAM_data = {
                "from" : from ? from : 0,
                "size" : 20,
                "query": {
                    "bool": {
                        "should": [
                            {
                                "match": EPAM_data_match
                            }
                        ]
                    }
                }
            };

            sendRequest(url, 'POST', EPAM_data, function(json){
                //new Column(json, 'hits.hits', url, true, from ? from : 0);
                new ColumnEpam(json, 'hits.hits', url, true, from ? from : 0);
            });
        };

        // column constructor for TM
        var Column = function(json, pathToArray, url, isEPAM, from){
            var self = this;
            self.page = parseInt(json['page']['number']); //current page number (taken from json)
            self.totalPages = parseInt(json['page']['totalPages']); // total page number (taken from json)
            self.url = url; // base url (with API key and keyword) without page parameter
            self.render = function(){
                var column = $('<div class="list-group"></div>'), // column wrapper
                    title = $('<a class="list-group-item active">Events</a>'), // column header
                    array = Object.byString(json, pathToArray), // get array of items
                    responseContainer = $('#response'); // column wrappoer in DOM



                responseContainer.empty(); // remove any previous columns
                //column.append(title); // append header to column wrapper

                for (var item in array){ // iterate through each item in array
                    var listItem = $('<a class="list-group-item row js_left_list"></a>'), // item wrapper
                        leftColumn = $('<div class="col-xs-12"></div>'), // wrapper left column
                        name = $('<div>' + '<b>name: </b>' + array[item].name + '</div>'), // item name
                        id = $('<div id="id">' + '<b>id : </b>' + array[item].id + '</div>'), // item id
                        itemUrl =  array[item].eventUrl; // item URL

                    listItem.data('id',array[item] );
                    //console.log(listItem.data );

                    leftColumn.append(name).append(id); // append name and id to wrapper left column
                    if (itemUrl) // apend link to TM if there is any to wrapper left column
                        leftColumn.append($('<a target="_blank" href="' + itemUrl + '"><b>Link to TM</b></div>'));
                    listItem.append(leftColumn); // append left column to item wrapper
                    column.append(listItem); // add whole item to column
                }
                self.previousPage = $('<a href="#" id="prev-page"' + (self.page <= 0 ? ('class="disabled"') : '') +  '></a>'); // previous page button
                self.nextPage = $('<a href="#" id="next-page"' + (self.page >= (self.totalPages - (isEPAM ? 0 : 1)) ? ('class="disabled"') : '') +  '></a>'); // next page button
                self.paging = $('<p id="paging">' + 'page ' + (self.page + 1) + ' of ' + (self.totalPages + (isEPAM ? 1 : 0)) + '</p>'); // display current page of total
                responseContainer.append(column).append(self.previousPage).append(self.nextPage).append(self.paging); // append all three bottom to column

                self.topSmallImg = $('<div class="img"><img src="">' + 'page ' + (self.page + 1) + ' of ' + (self.totalPages + (isEPAM ? 1 : 0)) + '</div>'); // display current page of total
            };
            self.setListeners = function(){
                self.previousPage.on('click', function(e){ // previous button click listener
                    e.preventDefault();
                    self.goToPreviousPage();
                });
                self.nextPage.on('click', function(e){ // next button click listener
                    e.preventDefault();
                    self.goToNextPage();
                });
                //$('.js_left_list')
                //.on('dblclick', function(e){ // show modal
                //    console.log('show modal now');
                //    fillModalFromItem(e.data);
                //    $('#myModal').modal({
                //        show: 'false'
                //    });
                //
                //})
                //.on('click', function(e){ // color","orange
                //    $(this).css("background-color","#f0f0f0");
                //});
            };
            self.goToPreviousPage = function(){ // forms url with correct previous page parameter, runs the query and builds new column with response data
                sendRequest(self.url + '&page=' + (self.page - 1), 'GET', null, function(response){
                    new Column(response, pathToArray, self.url, isEPAM);
                });

            };
            self.goToNextPage = function(){ // forms url with correct next page parameter, runs the query and builds new column with response data
                /**
                 * @url -string
                 * @method - string
                 * data - object
                 * callback - function
                 */
                sendRequest(self.url + '&page=' + (self.page + 1), 'GET', null ,function(response){
                    //console.log('***build new column',response);
                    new Column(response, pathToArray, self.url, isEPAM);
                });
            };
            self.render();
            self.setListeners();
        };

        // returns keyword
        var getKeywordValue = function(){
            return $('#text-to-search').val();
        };

        //universal ajax request sender
        var sendRequest = function(url, method, data, callback){
            spinner.show();
            //console.info('dataSend:' , data);

            $.ajax({
                type: method,
                url: url,
                async: true,
                data: data ? JSON.stringify(data) : "",
                success: function(response, textStatus, jqXHR) {
                    spinner.hide();
                    console.info('response ' , response);
                    callback(response);
                },
                error: function(xhr, status, err) {
                    spinner.hide();
                    showErrorPopup('Whoa! Method returned an error. :(');
                }
            });
        };

        // shows popup when error occured
        var showErrorPopup = function(message){
            var alert = $('#error-alert');
            alert.find('#error-message').text(message);
            alert.modal();
        };

        // shows popup info
        var fillModalFromItem = function(event) {
            $('#myModal .modal-body').empty();

            //  $('#myModal .modal-body').append(syntaxHighlight(JSON.stringify(event,null,2)));
            var node = new PrettyJSON.view.Node({
                el:$('#myModal .modal-body'),
                data:event
            });
            node.expandAll();
            $('#myModal .modal-body').append(node);


        };

        // column constructor for EPAM
        var ColumnEpam = function(json, pathToArray, url, isEPAM, from){
            var self = this;

            function itemInfoShow(itemInfo, leftColumn, columnRight, itemUrl){
                /*test id 2900502CFF8309E2*/
                if (itemInfo.flickrImages) { // apend img if it exist
                    var greenDot = $('<div class="green-dot"></div>'); // wrapper img
                    leftColumn.append(greenDot);

                    /*for(var i=0; i<itemInfo.length, i<3; i++){
                        if(!itemInfo.flickrImages[i]) continue;
                        columnRight.append($('<div class="crop-image col-xs-4"><a href="'+itemUrl+'" target="_blank"><img src="'+itemInfo.flickrImages[i]+'" class="img-responsive"></a></div>'));
                    }*/
                }

                /*if (itemInfo.universePage && itemInfo.flickrImages) { // apend img if it exist
                    var description = $('<h1 class="col-xs-12">'+ itemInfo.universePage.description + '</h1>');
                    columnRight.append(description).append($('<div class="col-xs-12"><a href="'+itemUrl+'"><img src="'+itemInfo.universePage.images[0]+'" class="img-responsive"></a></div>'));

                }*/

                // apend googleData.results if it exist
                /*if (itemInfo.googleData) {
                    columnRight.append( $(' <h3>googleData block</h3>') );

                    for(var i=0; i<itemInfo.googleData.results.length, i<3; i++){
                        //console.log('itemInfo.googleData.results', itemInfo.googleData.results[i].title);
                        columnRight.append( $('<div class="crop-image col-xs-12"> <h4>'+ itemInfo.googleData.results[i].title + '</h4> </div>') );
                    }
                }*/
            }


            function getEventsFlickrImagesCard(data){
                var $card = $("<div class='flickr_card'></div>"),
                    imageList = [],
                    limit = 17,
                    cardCount = 7;

                for(var item in data){
                    var ref, ref1;
                    if (data[item] != null ? (ref = data[item]._source) != null ? (ref1 = ref.info) != null ? ref1.flickrImages : void 0 : void 0 : void 0) {
                        imageList = _.concat(imageList, ref1.flickrImages);
                    }
                }

                imageList = _.uniq(imageList);
                imageList = _.slice(imageList, [start = 0], [end = limit]);

                if(imageList.length){
                    if(imageList.length > cardCount) cardCount = 12;
                    if(imageList.length > cardCount) cardCount = 17;
                    for (var _i = 0; _i < cardCount; _i++){
                        var $cardItem = $("<div class='flickr_card__item'><img alt='' src='./assets/empty_1x1.png'></div>");
                        if(imageList[_i])  $cardItem.css({backgroundImage: 'url(' + imageList[_i] +')'});
                        $card.append($cardItem);
                    }
                    $card.addClass('flickr_card-count_' + cardCount);
                }else{
                    return false;
                }
                return $card;
            }


            function setCalendar($appendTo, events, defaultDate){
                var calendar = $("<div id='calendar'></div>");
                $appendTo.append(calendar);
                setTimeout(function(){
                    calendar.fullCalendar({
                        header: {
                            left: 'prev,next today',
                            center: 'title'
                        },
                        defaultDate: defaultDate || undefined,
                        events: events || []
                    })
                }, 1);
            }

            self.page = from ; //current page number (taken from json)
            self.totalPages = parseInt(json['hits']['total']); // total page number (taken from json)
            console.log('self.paging', self.page,' of ',self.totalPages , 'totalItems' );
            //self.max_score = isEPAM ? (parseFloat(json['hits']['max_score'])) || 'none' : 'default';
            //console.log('self.max_score',self.max_score);
            self.url = url; // base url (with API key and keyword) without page parameter
            self.render = function(){
                var column = $('<div class="list-group"></div>'), // column wrapper
                    //title = $('<a class="list-group-item active">Events</a>'), // column header
                    array = Object.byString(json, pathToArray), // get array of items
                    responseContainer = $('#response'), // column wrappoer in DOM
                /*details column*/
                    columnRight = $('<div class="list-group-item row"></div>'),
                    titleRight = $('<a class="list-group-item active">Details card</a>'), // column header
                    responseDetailContainer = $('#response-detail'); // column wrappoer in DOM

                responseContainer.empty(); // remove any previous columns
                //column.append(title); // append header to column wrapper

                var flickrImagesCard = getEventsFlickrImagesCard(array);
                if(flickrImagesCard) columnRight.append(flickrImagesCard);


                if(array.length) {
                    var eventList = [],
                        startDates = [];
                    try {
                        for (var item in array){
                            var dates = array[item]['_source']['dates'];
                            if(_.isObject(dates)){
                                var event = {
                                    title: array[item]['_source'].name,
                                    start: dates.start.dateTime,
                                    end: dates.end.dateTime
                                }
                                eventList.push(event);
                                startDates.push(event.start);
                            }
                        }

                        var minDate,
                            currentDate = moment().unix();

                        for (var i = 0; i < startDates.length - 1; i++) {
                            startDates[i] = moment(startDates[i]).unix();
                        }

                        minDate = _.min(startDates);

                        if(minDate < currentDate){
                            var afterDates = [];
                            for (var i = 0; i < startDates.length - 1; i++) {
                                if(startDates[i] > currentDate){
                                    afterDates.push(startDates[i]);
                                }
                            }
                            minDate = _.min(afterDates);
                        }

                        if(eventList.length) setCalendar(columnRight, eventList, eventList[startDates.indexOf(minDate)].start);

                    }catch (err){
                        console.log(err);
                    }
                }



                if(array.length){
                    var rawAttractionList = [],
                        attractionList = [];
                    for (var item in array){
                        try {
                            var attractions = array[item]['_source']['_embedded']['attractions'];
                            if(_.isArray(attractions))
                                rawAttractionList = _.concat(rawAttractionList, attractions)
                        }
                        catch (err){
                            console.log(err);
                        }
                    }

                    
                    for(var item in rawAttractionList){
                        try {
                        if(rawAttractionList[item].image.url && rawAttractionList[item].name)
                            attractionList.push({url: rawAttractionList[item].image.url, name: rawAttractionList[item].name});
                        }
                        catch (e) {
                            console.log("!!!!!!!!! "+e.message);
                        }
                        
                    }

                    attractionList = _.filter(attractionList, function(_obj){
                        return _obj.url && _obj.name
                    });

                    attractionList = _.uniqBy(attractionList, 'url');
                    attractionList = _.slice(attractionList, [start = 0], [end = 2]);
                    self.initAttractionsCard(columnRight, attractionList, true);

                    columnRight.append('<div id="js_google_map" class="google_map"></div>');  // append Google maps
                }




                for (var item in array){ // iterate through each item in array
                    var listItem = $('<a class="list-group-item row js_left_list"></a>'), // item wrapper
                        leftColumn = $('<div class="col-xs-12"></div>'), // wrapper left column
                        name = $('<div>' + '<b>name: </b>' + array[item]['_source']['name'] + '</div>'), // item name
                        id = $('<div id="id">' + '<b>id : </b>' +  array[item]['_source']['id']  + '</div>'), // item id

                        itemUrl = array[item]['_source']['eventUrl'] , // item URL
                        itemInfo = array[item]['_source']['info'] || 'undefined item' ; // item info from EPAM only

                    listItem.data('id',array[item]['_source']['id'] );


                    leftColumn.append(name).append(id); // append name and id to wrapper left column
                    if (itemUrl) // apend link to TM if there is any to wrapper left column
                        leftColumn.append($('<a target="_blank" href="' + itemUrl + '"><b>Link to TM</b></div>'));

                    if(itemInfo) {
                        itemInfoShow(itemInfo, leftColumn , columnRight, itemUrl);
                    };

                    //columnRight.append('<hr/>');

                    listItem.append(leftColumn); // append left column to item wrapper
                    column.append(listItem); // add whole item to column
                }

                console.log(self.page, self.totalPages);

                self.previousPage = $('<a href="#" id="prev-page"' + (self.page <= 0 ? ('class="disabled"') : '') +  '></a>'); // previous page button
                self.nextPage = $('<a href="#" id="next-page"' + (self.page >= (self.totalPages - 20 ) ? ('class="disabled"') : '') +  '></a>'); // next page button
                self.paging = $('<p id="paging">' + 'page ' + ((self.page/20) + 1) + ' of ' + (( Math.floor(parseInt(self.totalPages)/20) )  + 1) + '</p>'); // display current page of total
                responseContainer.append(column);
                /*right column*/

                responseDetailContainer.empty(); // remove any previous columns

                console.log(self.totalPages, 'self.totalPages');
                if(self.totalPages > 0) {
                    responseContainer.append(self.previousPage).append(self.nextPage).append(self.paging); // append all three bottom to column
                    responseDetailContainer.append(columnRight);
                }else {
                    columnRight.empty();
                }

                self.topSmallImg = $('<div class="img"><img src="">' + 'page ' + (self.page + 1) + ' of ' + (self.totalPages + (isEPAM ? 1 : 0)) + '</div>'); // display current page of total
            };

            self.initAttractionsCard = function($appendTo, attractions, addSource){
                var source = 'http://s1.ticketm.net/tm/en-us',
                    $card = $("<div class='attraction_card attraction_card-" + attractions.length + "'></div>");
                for(var i in attractions){
                    var url = addSource ? source + attractions[i].url : attractions[i].url,
                        $cardItem = $("<div class='attraction_card__item'></div>");

                    if(attractions[i].name)
                        $cardItem.append("<div class='card_label'>" + attractions[i].name + "</div>");

                    if(url) $cardItem.css({backgroundImage: 'url(' + url +')'});
                    $card.append($cardItem);
                }

                $appendTo.append($card);
            };


            self.initMap = function(selectorId, center, zoom, markers){
                var map = new google.maps.Map(document.getElementById(selectorId), {
                    center: center,
                    zoom: 3
                });

                for (var i in markers){
                    new google.maps.Marker({
                        position: {
                            lat: markers[i].lat,
                            lng: markers[i].lng
                        },
                        map: map
                    });
                }

                return map;
            };

            self.renderCardDetail = function(responseDetailContainer , idDetail){
                var eventPicHost = 'http://s1.ticketm.net/tm/en-us/';
                var array = Object.byString(json, pathToArray), // get array of items
                /*details column*/
                    cardSingleRight = $('<div class="list-group-item row"></div>'),
                    titleCard = $('<a class="list-group-item active">Single card details</a>'); // column header

                //responseDetailContainer.append(titleCard);
                for (var item in array){
                    var source = array[item]['_source'],
                        idList = isEPAM ? source['id'] || 'undefined item' : 'not used in TM', // item info from EPAM only
                        itemAttractions = isEPAM ? source['_embedded']['attractions'] : 'not used in TM', //override item URL
                        attractionList = [],
                        itemInfo = isEPAM ? source['info'] || 'undefined item' : 'not used in TM'; // item info from EPAM only

                    //render cardSingleRight
                    if(idList === idDetail) {
                        console.log('fount id in list', idList , idDetail , itemAttractions);
                        if (itemInfo.flickrImages) { // apend img if it exist
                            responseDetailContainer.append($('<div class="col-xs-6 double-height"><img src="'+itemInfo.flickrImages[0]+'" class="img-responsive"><span></span></div>'));

                            for(var i=1; i<itemInfo.length, i<3; i++){
                                if(!itemInfo.flickrImages[i]) continue;
                                responseDetailContainer.append($('<div class="crop-image-flick col-xs-6"><img src="'+itemInfo.flickrImages[i]+'" class="img-responsive"></div>'));
                            }
                        }

                        responseDetailContainer.append('<div class="clearfix"></div>');

                        // Universe page
                        if(itemInfo.universePage){
                            if(itemInfo.universePage.description || itemInfo.universePage.images){
                                var universe = [
                                    {
                                        name: itemInfo.universePage.description,
                                        url: itemInfo.universePage.images[0]
                                    }
                                ];
                                self.initAttractionsCard(responseDetailContainer, universe, false);
                            }
                            console.log(itemInfo.universePage);
                        }

                        // Attractions
                        for(var item in itemAttractions){
                            try {
                            if(itemAttractions[item].image.url && itemAttractions[item].name)
                                attractionList.push({url: itemAttractions[item].image.url, name: itemAttractions[item].name});
                            } catch (e) {console.log("!!!!!!!!!"+e.message);}
                        }

                        attractionList = _.filter(attractionList, function(_obj){
                            return _obj.url && _obj.name
                        });

                        attractionList = _.uniqBy(attractionList, 'url');
                        attractionList = _.slice(attractionList, [start = 0], [end = 2]);
                        self.initAttractionsCard(responseDetailContainer, attractionList, true);

                        // Venues
                        if(_.isObject(itemInfo)){
                            if(_.isArray(itemInfo.venues)){
                                if(itemInfo.venues.length){
                                    var venue = [
                                        {
                                            name: itemInfo.venues[0].name,
                                            url: itemInfo.venues[0].image
                                        }
                                    ];
                                    self.initAttractionsCard(responseDetailContainer, venue, false);
                                }
                            }
                        }

                        // Map
                        if(_.isObject(source.location)){
                            responseDetailContainer.append('<div class="clearfix"></div>');
                            responseDetailContainer.append('<div id="js_google_map" class="google_map"></div>');
                            var center = {
                                lat: source.location.lat,
                                lng: source.location.lon
                            };
                            self.initMap('js_google_map', center, 2, [center]);
                        }

                        // Biography
                        if(itemInfo.attractions){
                            if(itemInfo.attractions[0]){
                                if(itemInfo.attractions[0].biography){
                                    var $card = $("<div class='text_card'></div>").html(itemInfo.attractions[0].biography).prepend("<h4>Biography</h4>");
                                    responseDetailContainer.append($card);
                                }
                            }
                        }

                    }

                }
                responseDetailContainer.append($('<hr>'));
            };
            self.setListeners = function(){
                self.previousPage.on('click', function(e){ // previous button click listener
                    e.preventDefault();
                    self.goToPreviousPage();
                });
                self.nextPage.on('click', function(e){ // next button click listener
                    e.preventDefault();
                    self.goToNextPage();
                });
                $('.js_left_list').on('click', function(e){ // list-group-item listener
                    var responseDetailContainer = $('#response-detail'),
                        responseContainer = $('#response .list-group-item'),
                        me = $(this),
                        idDetail = $(this).data('id');

                    e.preventDefault();
                    //console.log(me);
                    responseContainer.removeClass('active');
                    me.addClass('active');

                    responseDetailContainer.fadeOut(200, function() {
                        $(this).empty().show();
                        responseDetailContainer.fadeIn("slow");

                        self.renderCardDetail(responseDetailContainer, idDetail);
                    });
                });
            };
            self.goToPreviousPage = function(){ // forms url with correct previous page parameter, runs the query and builds new column with response data
                runEPAMRequest(from - 20);
                return;
            };
            self.goToNextPage = function(){ // forms url with correct next page parameter, runs the query and builds new column with response data
                runEPAMRequest(from + 20);
                return;

            };

            self.initGroupEventsMap = function(){
                var array = Object.byString(json, pathToArray);
                if(array.length){
                    var center = {lat: 39.3648338, lng: -101.4381589},
                        markers = [];

                    for (var item in array){
                        var location = array[item]['_source'].location;
                        if(location) markers.push({lat: location.lat, lng: location.lon});
                    }

                    self.initMap('js_google_map', center, 3, markers);
                }
            };

            self.render();
            self.initGroupEventsMap();
            self.setListeners();
        };

        function uniques(arr) {
            var a = [];
            for (var i=0, l=arr.length; i<l; i++)
                if (a.indexOf(arr[i]) === -1 && arr[i] !== '')
                    a.push(arr[i]);
            return a;
        }

        function load(query, cb) {
            var request = '{ "fields" : ["name"], "size":300, "query" :{ "match_phrase_prefix" : { "name":"' + query + '"}}}';
            sendRequest(search_keyword_EPAM_url, 'GET', request, function(json){
                if(json.hits && json.hits.hits && json.hits.hits.length > 0) {
                    var result = [];
                    for(var i in json.hits.hits) {
                        result.push(json.hits.hits[0]._source.name);
                    }
                    result = uniques(result);
                    cb(result);
                }
            });
        }

        var eventMatcher = function() {
            if (getApproach()==='TM'){
                $('.twitter-typeahead').css("display","block");
                return;
            }

            return function findMatches(q, cb) {
                var EPAM_data = {
                    "size" : 50,
                    "query": {
                        "match_phrase_prefix": {
                            "name": {
                                "query" : q
                            }
                        }
                    }
                };
                sendRequest(search_keyword_EPAM_url, 'POST', EPAM_data, function(json){
                    if(json.hits && json.hits.hits && json.hits.hits.length > 0) {
                        var result = [];
                        for(var i in json.hits.hits) {
//                            if(json.hits.hits[i].fields && json.hits.hits[i].fields.name && json.hits.hits[i].fields.name[0])
//                                result.push(json.hits.hits[i].fields.name[0]);
                                if(json.hits.hits[i]._source.name)
                                    result.push(json.hits.hits[i]._source.name);
                        }
                        //debugger
                        result = uniques(result);

                        cb(result);
                    }
                });
            }
        };

        var autoCompleteRunner = function() {
            if (getApproach() !== 'TM') {
                console.log(getApproach());
                $('#text-to-search').typeahead({
                        hint: true,
                        highlight: true,
                        minLength: 1
                    },
                    {
                        name: 'events',
                        source: eventMatcher()
                    });
                $('.twitter-typeahead').css("display", "block");

            }
        }


    });



})();
