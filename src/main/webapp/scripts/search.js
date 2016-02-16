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
        search_keyword_EPAM_url = 'http://localhost:9200/discovery/event/_search', // base URL for EPAM
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
        var getApproach = function(){
            var selected = $("input[type='radio'][name='approach']:checked");
            if (selected.length > 0)
                return selected.val();
            else
                return false;
        };

        // runs TM request
        var runTMRequest = function(){
            var keyword = getKeywordValue(),
                url = search_keyword_TM_url + (keyword ? ('&keyword=' + keyword) : '');

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
                    "_all": {
                        "query": splitted[0],
                        "operator": "or",
                        "fuzziness": 2
                    }
                }
            }
            else if (splitted.length > 1){
                EPAM_data_match[splitted[0]] = {
                    "query": splitted[1],
                    "operator": "or",
                    "fuzziness": 2
                };
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
                new Column(json, 'hits.hits', url, true, from ? from : 0);
            });
        };

        // column constructor for TM
        var Column = function(json, pathToArray, url, isEPAM, from){
            var self = this;
            self.page = isEPAM ? (from+1)/20 : parseInt(json['page']['number']); //current page number (taken from json)
            self.totalPages = isEPAM ? Math.floor(parseInt(json['total'] / 20)) : parseInt(json['page']['totalPages']); // total page number (taken from json)
            self.url = url; // base url (with API key and keyword) without page parameter
            self.render = function(){
                var column = $('<div class="list-group"></div>'), // column wrapper
                    title = $('<a class="list-group-item active">Events</a>'), // column header
                    array = Object.byString(json, pathToArray), // get array of items
                    responseContainer = $('#response'); // column wrappoer in DOM

                responseContainer.empty(); // remove any previous columns
                column.append(title); // append header to column wrapper

                for (var item in array){ // iterate through each item in array
                    var listItem = $('<a class="list-group-item row"></a>'), // item wrapper
                        leftColumn = $('<div class="col-xs-4"></div>'), // wrapper left column
                        name = $('<div>' + (isEPAM ? array[item]['_source']['name'] : array[item].name) + '</div>'), // item name
                        id = $('<div>' + (isEPAM ? array[item]['_source']['id'] : array[item].id) + '</div>'), // item id
                        itemUrl = isEPAM ? array[item]['_source']['eventUrl'] : array[item].eventUrl; // item URL

                    leftColumn.append(name).append(id); // append name and id to wrapper left column
                    if (itemUrl) // apend link to TM if there is any to wrapper left column
                        leftColumn.append($('<a target="_blank" href="' + itemUrl + '">Link to TM</div>'));
                    listItem.append(leftColumn); // append left column to item wrapper
                    column.append(listItem); // add whole item to column
                }
                self.previousPage = $('<a href="#" id="prev-page"' + (self.page <= 0 ? ('class="disabled"') : '') +  '></a>'); // previous page button
                self.nextPage = $('<a href="#" id="next-page"' + (self.page >= (self.totalPages - (isEPAM ? 0 : 1)) ? ('class="disabled"') : '') +  '></a>'); // next page button
                self.paging = $('<p id="paging">' + 'page ' + (self.page + 1) + ' of ' + (self.totalPages + (isEPAM ? 1 : 0)) + '</p>'); // display current page of total
                responseContainer.append(column).append(self.previousPage).append(self.nextPage).append(self.paging); // append all three above to column
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
            };
            self.goToPreviousPage = function(){ // forms url with correct previous page parameter, runs the query and builds new column with response data
                if (isEPAM){
                    runEPAMRequest(from - 20)
                }
                else {
                    sendRequest(self.url + '&page=' + (self.page - 1), function(response){
                        new Column(response, pathToArray, self.url, isEPAM);
                    });
                }
            };
            self.goToNextPage = function(){ // forms url with correct next page parameter, runs the query and builds new column with response data
                if (isEPAM){
                    runEPAMRequest(from + 20);
                }
                sendRequest(self.url + '&page=' + (self.page + 1), function(response){
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
            $.ajax({
                type: method,
                url: url,
                async: true,
                data: data ? JSON.stringify(data) : "",
                success: function(response, textStatus, jqXHR) {
                    spinner.hide();
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

    });
})();