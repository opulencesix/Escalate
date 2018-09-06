/*
 * Centralized management of user information.
 */
var Globals = require('../common/AppCommons');
var AppWebAPIUtils = require('../common/AppWebAPIUtils');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');
var Utils = require('../common/CommonUtils');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

var _isAuthenticated = false;
var _defaultUser = {
  name: Globals.ANONYMOUS
};

var _currentUser = assign({}, _defaultUser);

// Shortcut to avoid if's. Generally will not fail if localstorage has perms.
try {
  _currentUser.name = Utils.retrieveAuthPerm().name;
  Utils.storeAuth(Utils.retrieveAuthPerm());
  _isAuthenticated = true;
} catch (err) {
  console.log(err);
}

var UserStore = assign({}, EventEmitter.prototype, {
  initUser: function(userInfo) {
    _currentUser = userInfo;
  },

  getUser: function() {
    return _currentUser;
  },

  isAuthenticatedUser: function() {
    return _isAuthenticated;
  },

  setAuthenticatedUser: function(flag) {
    _isAuthenticated = flag;
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

  attemptRegister: function(regInfo) {
    AppWebAPIUtils.attemptRegister(regInfo);
  },

  attemptLogin: function(loginInfo) {
    AppWebAPIUtils.attemptLogin(loginInfo);
  },

  sendChangePasswordRequest: function(oldPassword, newPassword) {
    AppWebAPIUtils.changePassword(oldPassword, newPassword);
  },

  sendResetPasswordRequest: function(email) {
    AppWebAPIUtils.resetPassword(email);
  },

  logout: function() {
    Utils.clearAuth();
    this.initUser(_defaultUser);
    this.setAuthenticatedUser(false);
  }

});

UserStore.dispatchToken = AppDispatcher.register(function(payload) {

  var action = payload.action;
  switch (action.type) {
  case ActionTypes.ATTEMPT_REGISTER:
    UserStore.attemptRegister(action.regInfo);
    break;
  case ActionTypes.ATTEMPT_LOGIN:
    UserStore.attemptLogin(action.loginInfo);
    break;
  case ActionTypes.CHANGE_PASSWORD:
    UserStore.sendChangePasswordRequest(action.oldPassword, action.newPassword);
    UserStore.logout();
    UserStore.emitChange();
    break;
  case ActionTypes.RESET_PASSWORD:
    UserStore.sendResetPasswordRequest(action.email);
    break;
  case ActionTypes.LOGOUT:
    UserStore.logout();
    UserStore.emitChange();
    break;
  case ActionTypes.RECEIVE_REGISTER_RESPONSE:
    if (action.status === 'SUCCESS') {
      UserStore.initUser(action.userInfo);
      UserStore.setAuthenticatedUser(true);
      UserStore.emitChange();
    }
    break;
  default:
    // do nothing
  }

});

module.exports = UserStore;