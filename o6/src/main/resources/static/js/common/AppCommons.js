/*
 * Global constants and functions. Not general utility functions.
 */
var keymirror = require('keymirror');
var AppConfig = require('./AppConfig');

module.exports = {
  AppConfig: AppConfig,
  APP_NAME: 'O6',
  ANONYMOUS: 'Anonymous',
  UNKNOWN_USER_ID: 'UNKNOWN_USER_ID',
  DEFAULT_ROLE: 'General',
  YOUTUBE_VIDEO: 'youtubevideo',
  YOUTUBE_PLAYLIST: 'youtubeplaylist',
  TOKEN_REFRESH_SECS: 3600,
  
  // Youtube player related
  PLAYER_ID_PREFIX: 'ytplayer',
  playerSeqID: 0,
  //A helper function that loads the Youtube API. It will make sure the API
  //is only loaded once even if it's called multiple times.
  ytLoadAPI: (function() {
    var status = null;
    var callbacks = [];

    function onload() {
      status = 'loaded';
      while (callbacks.length) {
        callbacks.shift()();
      }
    }

    return function(callback) {
      if (status === 'loaded') {
        setTimeout(callback, 0);
        return;
      }

      callbacks.push(callback);
      if (status === 'loading') { return; }

      status = 'loading';
      var script = document.createElement('script');
      script.src = 'https://www.youtube.com/iframe_api';
      window.onYouTubeIframeAPIReady = onload;
      var firstScriptTag = document.getElementsByTagName('script')[0];
      firstScriptTag.parentNode.insertBefore(script, firstScriptTag);
    };
  })(),

  // Various React event constants
  PayloadSources: keymirror({
    SERVER_ACTION: null,
    VIEW_ACTION: null
  }),

  ActionTypes: keymirror({
    // Receive content from server
    RECEIVE_UPCOMING_CONTENT: null,
    RECEIVE_CONSUMED_CONTENT: null,
    RECEIVE_INPROGRESS_CONTENT: null,
    RECEIVE_FAVORITE_CONTENT: null,
    RECEIVE_SPECIAL_CONTENT: null,
    RECEIVE_SEARCH_CONTENT: null,

    // Receive misc content
    RECEIVE_NOTIFICATIONS: null,
    
    // Content view related.
    CONTENT_VIEW_STARTED: null,
    UPCOMING_CONTENT_SEEN: null,
    UPCOMING_CONTENT_SKIPPED: null,
    CONTENT_SEEN: null,
    CONTENT_FAVORITE_TOGGLED: null,
    STAND_ALONE_CONTENT_ID_CHANGED: null,
    SEARCH_CONTENT: null,

    // Login and registration related
    ATTEMPT_REGISTER: null,
    ATTEMPT_LOGIN: null,
    RESET_PASSWORD: null,
    LOGOUT: null,
    RECEIVE_REGISTER_RESPONSE: null,
    UI_REGISTER_UNLOAD: null,

    // Profile related
    RECEIVE_PROFILE_SCHEMA: null,
    PROFILE_UPDATE: null,
    PROFILE_MODIFY: null,
    PROFILE_DELETE: null,
    RECEIVE_PROFILES: null,
    
    // Feedback
    SUBMIT_FEEDBACK: null,
    
    // Content Modules
    RECEIVE_CONTENT_MODULE_INFO: null,
    PURCHASE_CONTENT_MODULE: null

  })

};
