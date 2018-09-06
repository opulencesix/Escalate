/*
 * Relay user store manipulation actions from user and profile 
 * related view components
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Globals = require('../common/AppCommons');

var ActionTypes = Globals.ActionTypes;

module.exports = {

  attemptToRegister: function(regInfo) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.ATTEMPT_REGISTER,
      regInfo: regInfo
    });
  },
  
  attemptToLogin: function(loginInfo) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.ATTEMPT_LOGIN,
      loginInfo: loginInfo
    });
  },

  changePassword: function(oldPassword, newPassword) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.CHANGE_PASSWORD,
      oldPassword: oldPassword,
      newPassword: newPassword
    });
  },

  resetPassword: function(email) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.RESET_PASSWORD,
      email: email
    });
  },

  logout: function() {
    AppDispatcher.handleViewAction({
      type: ActionTypes.LOGOUT
    });
  },
  
  profileUpdate: function(newProfile) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.PROFILE_UPDATE,
      profile: newProfile
    });
  },
  
  profileModify: function(newProfile) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.PROFILE_MODIFY,
      profile: newProfile
    });
  },
  
  profileDelete: function(profileName) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.PROFILE_DELETE,
      profileName: profileName
    });
  },
  
  searchContent: function(searchStr) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.SEARCH_CONTENT,
      searchStr: searchStr
    });    
  },
  
  submitFeedback: function(feedbackType, feedbackStr) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.SUBMIT_FEEDBACK,
      feedbackType: feedbackType,
      feedbackStr: feedbackStr
    });    
  }
  

};