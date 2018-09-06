/*
 * Action creators to relay responses from server.
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Globals = require('../common/AppCommons');

var ActionTypes = Globals.ActionTypes;

/* This is a global within the module because the caller functions for this might not be
 * called by module.functionX. Instead that functionX e.g. receiveUpcomingContent might
 * be stored somewhere else and called. So, this.receiveContent will not work.
 */
function receiveContent(actionType, content) {
  AppDispatcher.handleServerAction({
    type: actionType,
    content: content
  });
}

module.exports = {

  /*
   * Receive content list for various types of content.
   */
  receiveUpcomingContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_UPCOMING_CONTENT, content);
  },

  receiveConsumedContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_CONSUMED_CONTENT, content);
  },

  receiveInProgressContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_INPROGRESS_CONTENT, content);
  },

  receiveFavoriteContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_FAVORITE_CONTENT, content);
  },

  receiveSpecialContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_SPECIAL_CONTENT, content);
  },

  receiveSearchContent: function(content) {
    receiveContent(ActionTypes.RECEIVE_SEARCH_CONTENT, content);
  },

  receiveProfileSchema: function(profileSchema) {
    AppDispatcher.handleServerAction({
      type: ActionTypes.RECEIVE_PROFILE_SCHEMA,
      profileSchema: profileSchema
    });
  },

  receiveProfiles: function(profiles) {
    AppDispatcher.handleServerAction({
      type: ActionTypes.RECEIVE_PROFILES,
      profiles: profiles
    });
  },

  receiveRegisterResponse: function(userInfo, status, msg) {
    AppDispatcher.handleServerAction({
      type: ActionTypes.RECEIVE_REGISTER_RESPONSE,
      userInfo: userInfo,
      status: status,
      msg: msg
    });
  },
  
  receiveNotifications: function(notifications) {
    AppDispatcher.handleServerAction({
      type: ActionTypes.RECEIVE_NOTIFICATIONS,
      notifications: notifications
    });
  },
  
  receiveContentModules: function(userContentModules) {
    AppDispatcher.handleServerAction({
      type: ActionTypes.RECEIVE_CONTENT_MODULE_INFO,
      subscribedModules: userContentModules.subscribedModules,
      unSubscribedModules: userContentModules.unSubscribedModules
    });    
  }



};