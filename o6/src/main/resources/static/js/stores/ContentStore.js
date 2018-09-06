/*
 * Store content related information. Current content, watched, favorites, etc.
 * Split into more later as needed.
 */
var Globals = require('../common/AppCommons');
var AppWebAPIUtils = require('../common/AppWebAPIUtils');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Utils = require('../common/CommonUtils');
var EventEmitter = require('events').EventEmitter;
var ProfileStore = require('./ProfileStore');
var assign = require('object-assign');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

// This map is used to store details of all content received, so that all
// remaining code can work just on Ids, and get details from this common data.
var _idContentMap = {};

var _upcomingContentIds = [];
var _lastUpcomingContentLoadStatus = "SUCCESS";

var _seenContentIds = Object.create(null);
var _inProgressContentIds = Object.create(null);
var _favoriteContentIds = Object.create(null);
var _specialContentIds = Object.create(null);
var _searchContentIds = Object.create(null);

var _standAloneContentId = null;

var ContentStoreCustomObject = {
  clear: function() {
    _upcomingContentIds = [];
    _seenContentIds = Object.create(null);
    _inProgressContentIds = Object.create(null);
    _favoriteContentIds = Object.create(null);
    _specialContentIds = Object.create(null);
    _searchContentIds = Object.create(null);
  },

  setUpcoming: function(upcomingContent) {
    // Keep the first element since it might be being currently
    // viewed. Also, if the upcoming content on the server is not
    // the latest due to offline updates, filter content that might
    // have been seen in the meanwhile.
    var firstUpcomingContentId = _upcomingContentIds[0];
    _upcomingContentIds = _upcomingContentIds.slice(0, 1).concat(
            upcomingContent.map(function(contentElem) {
              return contentElem.contentId;
            }).filter(function(elem) {
              // return !_inProgressContentIds[elem] &&
              // !_seenContentIds[elem] && .... for now, repeat telecast is ok.
              return (!firstUpcomingContentId || firstUpcomingContentId !== elem);
            }));
    upcomingContent.forEach(function(elem) {
      _idContentMap[elem.contentId] = elem;
    });

    if (!upcomingContent || upcomingContent.length == 0) {
      _lastUpcomingContentLoadStatus = "EMPTY";
    } else {
      _lastUpcomingContentLoadStatus = "SUCCESS";
    }
  },

  /*
   * Update the content lists for various content types like favorites, consumed
   * etc.
   */
  setConsumed: function(content) {
    content.forEach(function(elem) {
      _seenContentIds[elem.contentId] = true;
      _idContentMap[elem.contentId] = elem;
    });
  },

  setInProgress: function(content) {
    content.forEach(function(elem) {
      _inProgressContentIds[elem.contentId] = true;
      _idContentMap[elem.contentId] = elem;
    });
  },

  setFavorite: function(content) {
    content.forEach(function(elem) {
      _favoriteContentIds[elem.contentId] = true;
      _idContentMap[elem.contentId] = elem;
    });
  },

  setSpecial: function(content) {
    content.forEach(function(elem) {
      _specialContentIds[elem.contentId] = true;
      _idContentMap[elem.contentId] = elem;
    });
  },

  setSearchContent: function(content) {
    content.forEach(function(elem) {
      _searchContentIds[elem.contentId] = true;
      _idContentMap[elem.contentId] = elem;
    });
  },

  getStandAloneContentId: function() {
    return _standAloneContentId;
  },
  
  updateStandAloneContentId: function(id) {
    _standAloneContentId = id;
  },
  
  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  },

  getContentDetails: function(id) {
    return _idContentMap[id];
  },

  getNextUpcomingId: function() {
    var currProfile = ProfileStore.getCurrentProfile();
    return _upcomingContentIds.length > 0 ? _upcomingContentIds[0] : null;
  },

  getLastFetchStatus: function() {
    return _lastUpcomingContentLoadStatus;
  },

  // Ideally we should not need this, but Flux does not allow
  // a dispatch while another dispatch is going on. So, if we are
  // displaying upcoming content, then setting the InProgress cannot
  // be done in another dispatch within it.
  getNextUpcomingIdAndMarkInProgress: function() {
    var id = this.getNextUpcomingId();
    if (id) {
      this.markInProgress(id);
    }
    return id;
  },

  getContentListFromIds: function(ids) {
    return ids.map(function(id) {
      return _idContentMap[id];
    });
  },

  /*
   * Various getters.
   */
  getSeenContent: function() {
    return this.getContentListFromIds(Object.keys(_seenContentIds));
  },

  getInProgressContent: function() {
    return this.getContentListFromIds(Object.keys(_inProgressContentIds));
  },

  getFavoriteContent: function() {
    return this.getContentListFromIds(Object.keys(_favoriteContentIds));
  },

  getSpecialContent: function() {
    return this.getContentListFromIds(Object.keys(_specialContentIds));
  },

  getSearchContent: function() {
    return this.getContentListFromIds(Object.keys(_searchContentIds));
  },

  /*
   * Relay the content view related events back, and update in-memory
   * structures.
   */
  markInProgress: function(id) {
    _inProgressContentIds[id] = true;
    AppWebAPIUtils.recordContentEvent(id, _idContentMap[id].language,
            "inProgress");
  },

  markContentConsumed: function(id) {
    _seenContentIds[id] = true;
    delete _inProgressContentIds[id];
    AppWebAPIUtils.recordContentEvent(id, _idContentMap[id].language,
            "consumed");
  },

  handleUpcomingSkipped: function(id) {
    _upcomingContentIds.splice(0, 1);
    if (_upcomingContentIds.length <= 1) {
      AppWebAPIUtils.getUpcomingContentForProfile(ProfileStore
              .getCurrentProfile());
    }
  },

  markUpcomingConsumed: function(id) {
    _upcomingContentIds.splice(0, 1);
    _seenContentIds[id] = true;
    delete _inProgressContentIds[id];
    if(_upcomingContentIds.length <= 1) {
      AppWebAPIUtils.recordConsumedAndPrefetch(id, _idContentMap[id].language,
              ProfileStore.getCurrentProfile());      
    } else {
      AppWebAPIUtils.recordContentEvent(id, _idContentMap[id].language, "consumed");
    }
  },

  markContentFavoriteToggled: function(id) {
    if(!_favoriteContentIds[id]) {
      _favoriteContentIds[id] = true;
      _idContentMap[id].favorite = true;
    } else {
      delete _favoriteContentIds[id];
      _idContentMap[id].favorite = false;
    }
    AppWebAPIUtils.recordContentEvent(id, _idContentMap[id].language,
            "toggleFavorite");
  }

};

var ContentStore = assign({}, EventEmitter.prototype, ContentStoreCustomObject);

ContentStore.dispatchToken = AppDispatcher.register(function(payload) {

  var action = payload.action;
  switch (action.type) {

  // Actions from backend
  case ActionTypes.RECEIVE_UPCOMING_CONTENT:
    ContentStore.setUpcoming(action.content);
    ContentStore.emitChange();
    break;
  case ActionTypes.RECEIVE_CONSUMED_CONTENT:
    ContentStore.setConsumed(action.content);
    ContentStore.emitChange();
    break;
  case ActionTypes.RECEIVE_INPROGRESS_CONTENT:
    ContentStore.setInProgress(action.content);
    ContentStore.emitChange();
    break;
  case ActionTypes.RECEIVE_FAVORITE_CONTENT:
    ContentStore.setFavorite(action.content);
    ContentStore.emitChange();
    break;
  case ActionTypes.RECEIVE_SPECIAL_CONTENT:
    ContentStore.setSpecial(action.content);
    ContentStore.emitChange();
    break;
  case ActionTypes.RECEIVE_SEARCH_CONTENT:
    ContentStore.setSearchContent(action.content);
    ContentStore.emitChange();
    break;

  // Actions from UI
  case ActionTypes.UPCOMING_CONTENT_SEEN:
    ContentStore.markUpcomingConsumed(action.id);
    ContentStore.emitChange();
    break;
  case ActionTypes.UPCOMING_CONTENT_SKIPPED:
    ContentStore.handleUpcomingSkipped(action.id);
    ContentStore.emitChange();
    break;
  case ActionTypes.CONTENT_SEEN:
    ContentStore.markContentConsumed(action.id);
    ContentStore.emitChange();
    break;
  case ActionTypes.CONTENT_FAVORITE_TOGGLED:
    ContentStore.markContentFavoriteToggled(action.id);
    ContentStore.emitChange();
    break; 
  case ActionTypes.STAND_ALONE_CONTENT_ID_CHANGED:
    ContentStore.updateStandAloneContentId(action.id);
    ContentStore.emitChange();
    break; 
  case ActionTypes.CONTENT_VIEW_STARTED:
    ContentStore.markInProgress(action.id);
    break;
  case ActionTypes.SEARCH_CONTENT:
    _searchContentIds = Object.create(null);
    AppWebAPIUtils.searchContent(action.searchStr);
    break;
  case ActionTypes.RECEIVE_REGISTER_RESPONSE:
    if (action.status === 'SUCCESS') {
      ContentStore.clear();
    }
    break;
  case ActionTypes.PROFILE_DELETE:
  case ActionTypes.PROFILE_UPDATE:
  case ActionTypes.RECEIVE_PROFILES:
    AppDispatcher.waitFor([ProfileStore.dispatchToken]);
    ContentStore.clear();
    AppWebAPIUtils.getUpcomingContentForProfile(ProfileStore
            .getCurrentProfile());
    AppWebAPIUtils.getInProgressContent();
    AppWebAPIUtils.getConsumedContent();
    AppWebAPIUtils.getFavoriteContent();
    AppWebAPIUtils.getSpecialContent();
    break;
  default:
    // do nothing
  }

});

module.exports = ContentStore;