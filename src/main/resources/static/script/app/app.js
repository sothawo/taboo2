/*
 Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

 Licensed under the Apache License, Version 2.0 (the 'License');
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an 'AS IS' BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
// define additional functions
if (typeof String.prototype.startsWith != 'function') {
    String.prototype.startsWith = function (str) {
        return this.lastIndexOf(str, 0) === 0;
    };
}

var app = angular.module('taboo', ['base64']);

// set configuration for the backend service
app.constant('tabooService', {
    //urlService: 'http://localhost:8080',
    // leave urlService empty to use the same host and port was used in calling the app
    urlService: '',
    pathBookmarks: '/taboo2/bookmarks',
    pathTags: '/taboo2/tags',
    pathTitle: '/taboo2/title',
    pathCheck: '/taboo2/check'
});

// create Controller that creates a ViewModel
app.controller('TabooCtrl', function ($scope, $http, $base64, $location, tabooService) {
    $scope.vm = new TabooVM($http, $base64, $location, tabooService);
});


// ViewModel
function TabooVM($http, $base64, $location, tabooService) {
    var self = this;
    /** entry for new bookmark's url. */
    this.newBookmarkUrl = '';
    /** entry for new bookmark's title. */
    this.newBookmarkTitle = '';
    /** entry for the new bookmark's tags. */
    this.newBookmarkTags = '';
    /** entry for the bookmark's id when editing */
    this.editBookmarkId = '';
    /** the bookmarks to show. */
    this.bookmarks = [];
    /** search field for bookmarks. */
    this.searchText = '';
    /** the list of selected tags. */
    this.selectedTags = new TabooSet();
    /** the list of available tags */
    this.availableTags = new TabooSet();

    /** the username and password */
    this.username = '';
    this.password = '';
    /** the authentication state ('success', any other value is not authenticated */
    this.authenticated = '';

    // check if there is already an authentication header in the localstore
    if(localStorage) {
        var authHeader = localStorage.getItem('taboo2AuthHeader');
        if(authHeader) {
            $http.defaults.headers.common['Authorization'] = authHeader;
            this.authenticated = 'success';

        }
    }
    /** flag wether new bookmark panel content is visible. */
    this.newBookmarkVisible = false;

    /**
     * calls the backend with the given credential. whenn succesful, stores the auth header an sets the
     * authenticated state.
     */
    this.tryLogin = function() {
        var authHeader = 'Basic ' + $base64.encode(self.username + ':' + self.password);
        var headers = {'Authorization': authHeader};
        $http
            .get(tabooService.urlService + tabooService.pathCheck, {headers: headers})
            .then(
                function (response) {
                    if(localStorage) {
                        localStorage.setItem('taboo2AuthHeader', authHeader);
                    }
                    $http.defaults.headers.common['Authorization'] = authHeader;
                    self.authenticated = 'success';
                    self.clearSelection();
                },
                function (response) {
                    alert('Error: ' + response.status + ' ' + response.statusText + ' (' + response.data + ')');
                }
            );
    }

    /**
     * reset the login information.
     */
    this.logout = function() {
        delete $http.defaults.headers.common['Authorization'];
        if(localStorage) {
            localStorage.removeItem('taboo2AuthHeader');
        }
        self.authenticated = '';
        self.username = '';
        self.password = '';
    }

    /**
     * toggles that flag that determines the visibility of the new bookmark entry.
     */
    this.toggleNewBookmarkVisibility = function() {
        self.newBookmarkVisible = !self.newBookmarkVisible;
    }

    /**
     * clears all selection data and ses the selected tags to empty.
     */
    this.clearSelection = function () {
        self.searchText = '';
        self.setSelectedTags([]);
    }

    /**
     * sets the selected tags and reloads the bookmarks for the tags.
     * @param tags the new selected tags.
     */
    this.setSelectedTags = function (tags) {
        if (tags && tags.isSetObject) {
            self.selectedTags = tags;
        } else if (angular.isArray(tags)) {
            self.selectedTags = new TabooSet(tags);
        } else {
            self.selectedTags = new TabooSet();
        }
        self.reloadBookmarks();
    }

    /**
     * sets the bookmarks to show and updates the tag sets.
     * @param bookmarks
     */
    this.setBookmarksToShow = function (bookmarks) {
        self.bookmarks = bookmarks;
        // collect the tags from the bookmarks
        var tags = new TabooSet();
        var count = self.bookmarks.length;
        if (count > 0) {
            for (var i = 0; i < count; i++) {
                var bookmark = self.bookmarks[i];
                tags = tags.union(bookmark.tagsSet);
            }

            // remove the selected tags and set the result as available
            self.availableTags = tags.difference(self.selectedTags);
        }
    };

    /**
     * reloads the bookmarks for the given selection criteria
     */
    this.reloadBookmarks = function () {
        // search parameters
        var paramsAreSet = false;
        var params = {};
        if (self.selectedTags.size() > 0) {
            params['tag'] = self.selectedTags.getElements();
            paramsAreSet = true;
        }
        if (self.searchText) {
            params['search'] = self.searchText;
            paramsAreSet = true;
        }

        if (paramsAreSet) {
            $http.get(tabooService.urlService + tabooService.pathBookmarks, {params: params})
                .then(function (result) {
                    var bookmarks = [];
                    var i = 0;
                    while (i < result.data.length) {
                        var bookmark = new Bookmark(result.data[i]);
                        bookmarks.push(bookmark);
                        i++;
                    }
                    self.setBookmarksToShow(bookmarks);
                }).catch(function (result) {
                alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
            });
        } else {
            // only get the tags
            $http.get(tabooService.urlService + tabooService.pathTags)
                .then(function (result) {
                    self.setBookmarksToShow([]);
                    self.availableTags = new TabooSet(result.data);
                }).catch(function (result) {
                alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
            });
        }
    };

    /**
     * adds a tag to the selected tag list and reloads the bookmarks.
     * @param tag
     */
    this.addTagToSelection = function (tag) {
        self.selectedTags.add(tag);
        self.reloadBookmarks();
    };

    /**
     * removes a tag from the selected tags list and reloads the bookmarks.
     * @param tag
     */
    this.removeTagFromSelection = function (tag) {
        self.selectedTags.remove(tag);
        self.reloadBookmarks();
    };

    /**
     * tries to load the title for current new bookmark url.
     */
    this.loadTitle = function () {
        if (self.newBookmarkUrl) {
            $http.get(tabooService.urlService + tabooService.pathTitle, {params: {url: self.newBookmarkUrl}})
                .then(function (result) {
                    self.newBookmarkUrl = result.data.url;
                    self.newBookmarkTitle = result.data.title;
                }).catch(function (result) {
                alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
            });
        }
    };

    /**
     * saves a new or updated bookmark to the backend. tags are split by whitespaces comma, colon or semicolon.
     */
    this.saveEntryData = function () {
        if (self.newBookmarkUrl) {
            var bookmark = new Bookmark();
            bookmark.url = self.newBookmarkUrl;
            bookmark.title = self.newBookmarkTitle;
            bookmark.tags = self.newBookmarkTags
                .toLowerCase()
                .split(/[\s*,;:]/i)
                .filter(function (t) {
                    return !(t === '');
                });

            if (self.editBookmarkId) {
                bookmark.id = self.editBookmarkId;
                $http.put(tabooService.urlService + tabooService.pathBookmarks, bookmark)
                    .then(function (result) {
                        self.setSelectedTags(bookmark.tags);
                    }).catch(function (result) {
                    alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
                });
            } else {
                $http.post(tabooService.urlService + tabooService.pathBookmarks, bookmark)
                    .then(function (result) {
                        var createdBookmark = result.data;
                        self.setSelectedTags(createdBookmark.tags);
                    }).catch(function (result) {
                    alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
                });
            }
        }
    };

    /**
     * clears the entry fields for new bookmarks.
     */
    this.clearEntryData = function () {
        self.newBookmarkUrl = '';
        self.newBookmarkTitle = '';
        self.newBookmarkTags = '';
        self.editBookmarkId = undefined;
    };

    /**
     * deletes a bookmark from the backend.
     * @param bookmark the bookmark to delete
     */
    this.deleteBookmark = function (bookmark) {
        if (bookmark) {
            // todo: confirm
            bootbox.confirm('Are you sure to delete ' + bookmark.url + '?',
                function(confirmed) {
                    if(confirmed) {
                        $http.delete(tabooService.urlService + tabooService.pathBookmarks + '/' + bookmark.id)
                            .then(function (result) {
                                self.reloadBookmarks();
                            })
                            .catch(function (result) {
                                alert('Error: ' + result.status + ' ' + result.statusText + ' (' + result.data + ')');
                            });
                    }
                });
        }
    };

    /**
     * sets the data of the given bookmark in the controls for editing.
     * @param bookmark the bookmark to edit
     */
    this.editBookmark = function (bookmark) {
        if (bookmark) {
            self.newBookmarkUrl = bookmark.url;
            self.newBookmarkTitle = bookmark.title;
            self.newBookmarkTags = bookmark.joinedTags();
            self.editBookmarkId = bookmark.id;
            self.newBookmarkVisible = true;
        }
    };

    // check for search arguments
    var searchObject = $location.search();
    if(searchObject && searchObject.newBookmarkUrl) {
        this.newBookmarkUrl = searchObject.newBookmarkUrl;
        this.newBookmarkVisible = true;
        this.loadTitle();
    }

    // when already authenticated
    if(this.authenticated == 'success'){
        this.clearSelection();
    }
}


/**
 * creates a Bookmark from a REST-data Bookmark (these have an array of tags, not a Set).
 * @param restBookmark bookmark from a REST call.
 * @constructor
 */
function Bookmark(restBookmark) {
    var self = this;
    if (restBookmark) {
        this.id = restBookmark.id;
        this.url = restBookmark.url;
        this.title = restBookmark.title;
        this.tags = restBookmark.tags;
        this.tagsSet = new TabooSet(restBookmark.tags);
    } else {
        this.id = undefined;
        this.url = '';
        this.title = ''
        this.tags = [];
        this.tagsSet = new TabooSet();
    }

    if (!(this.url.startsWith('http://') || this.url.startsWith('https://'))) {
        this.urlWithPrefix = 'http://' + this.url;
    } else {
        this.urlWithPrefix = this.url;
    }

    this.joinedTags = function () {
        return self.tagsSet.getElements().join(', ');
    };
}
