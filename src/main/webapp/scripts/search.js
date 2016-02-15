(function(){

    Object.byString = function(o, s) {
        s = s.replace(/\[(\w+)\]/g, '.$1'); // convert indexes to properties
        s = s.replace(/^\./, '');           // strip a leading dot
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

    var search_keyword_TM_url = 'https://app.ticketmaster.com/discovery/v1/events.json?apikey=7elxdku9GGG5k8j0Xm8KWdANDgecHMV0',
        spinner;

    $(document).ready(function(){

        spinner = $('#spinner');

        $('#search-btn').on('click', function(e){
            e.preventDefault();
            var approach = getApproach();
            if (approach === "TM")
                runTMRequest(); // run TM request
            else
                runEPAMRequest(); // run EPAM request
        });

        $('#text-to-search').on('keyup', function(e){
            var input = $(e.target);
            if (e.keyCode == 13){
                input.blur();
                $('#search-btn').trigger('click');
            }
        });

        // get approach: 'TM' or 'EPAM'
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

            sendRequest(url, function(json){
                new Column(json, '_embedded.events', url);
            });
        };

        // column constructor
        var Column = function(json, pathToArray, url){
            var self = this;
            self.page = parseInt(json['page']['number']);
            self.totalPages = parseInt(json['page']['totalPages']);
            self.url = url;
            self.render = function(){
                var column = $('<div class="list-group"></div>'), //subcolumn future element
                    title = $('<a class="list-group-item active">Events</a>'),
                    array = Object.byString(json, pathToArray),
                    responseContainer = $('#response');

                responseContainer.empty();
                column.append(title);

                for (var item in array){
                    var listItem = $('<a class="list-group-item row"></a>'),
                        leftColumn = $('<div class="col-xs-4"></div>'),
                        name = $('<div>' + array[item].name + '</div>'),
                        id = $('<div>' + array[item].id + '</div>');

                    leftColumn.append(name).append(id);
                    if (array[item].eventUrl) //apend link to TM if there is any
                        leftColumn.append($('<a target="_blank" href="' + array[item].eventUrl + '">Link to TM</div>'));
                    listItem.append(leftColumn);
                    column.append(listItem);
                }
                self.previousPage = $('<a href="#" id="prev-page"' + (self.page <= 0 ? ('class="disabled"') : '') +  '></a>');
                self.nextPage = $('<a href="#" id="next-page"' + (self.page >= (self.totalPages - 1) ? ('class="disabled"') : '') +  '></a>');
                self.paging = $('<p id="paging">' + 'page ' + (self.page + 1) + ' of ' + self.totalPages + '</p>');
                responseContainer.append(column).append(self.previousPage).append(self.nextPage).append(self.paging);
            };
            self.setListeners = function(){
                self.previousPage.on('click', function(e){
                    e.preventDefault();
                    self.goToPreviousPage();
                });
                self.nextPage.on('click', function(e){
                    e.preventDefault();
                    self.goToNextPage();
                });
            };
            self.goToPreviousPage = function(){
                sendRequest(self.url + '&page=' + (self.page - 1), function(response){
                    new Column(response, pathToArray, self.url);
                });
            };
            self.goToNextPage = function(){
                sendRequest(self.url + '&page=' + (self.page + 1), function(response){
                    new Column(response, pathToArray, self.url);
                });
            };
            self.render();
            self.setListeners();
        };

        // runs EPAM request
        var runEPAMRequest = function(){
            //?
        };

        // returns keyword
        var getKeywordValue = function(){
            return $('#text-to-search').val();
        };

        //universal ajax request sender
        var sendRequest = function(url, callback){
            spinner.show();
            $.ajax({
                type: "GET",
                url: url,
                async: true,
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