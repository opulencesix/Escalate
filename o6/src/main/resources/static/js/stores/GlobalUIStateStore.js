/*
 * This store is to store global UI state that is transient in 
 * nature, such as open modal boxes, spinners, error messages shown
 * inline, etc. that depend upon responses from the server corresponding
 * to a previous action. In other cases of UI manipulation, the state need
 * not be stored specially, flux handles those well.
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppWebAPIUtils = require('../common/AppWebAPIUtils');
var Globals = require('../common/AppCommons');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');
var UserStore = require('./UserStore');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

// Login/Register or change password in progress
var _waitingForRegisterStatus = false;
var _lastServerStatus = '';
var _srvMsg = '';

var Store = assign({}, EventEmitter.prototype, {
  getWaitingForRegisterFlag: function() {
    return _waitingForRegisterStatus;
  },

  setWaitingForRegisterFlag: function(flag) {
    _waitingForRegisterStatus = flag;
  },

  getLastServerStatus: function() {
    return _lastServerStatus;
  },

  getSrvMsg: function() {
    return _srvMsg;
  },

  setLastServerStatus: function(status, msg) {
    _lastServerStatus = status;
    _srvMsg = msg;
  },
  
  submitFeedback: function(feedbackType, feedbackStr) {
    AppWebAPIUtils.submitFeedback(feedbackType, feedbackStr);
  },


  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

});

Store.dispatchToken = AppDispatcher.register(function(payload) {
  AppDispatcher.waitFor([UserStore.dispatchToken]);

  var action = payload.action;
  switch (action.type) {

  case ActionTypes.ATTEMPT_REGISTER:
  case ActionTypes.ATTEMPT_LOGIN:
    Store.setWaitingForRegisterFlag(true);
    Store.setLastServerStatus('', '');
    Store.emitChange();
    break;
  case ActionTypes.SUBMIT_FEEDBACK:
    Store.submitFeedback(action.feedbackType, action.feedbackStr);
    break;
  case ActionTypes.UI_REGISTER_UNLOAD:
    Store.setWaitingForRegisterFlag(false);
    Store.setLastServerStatus('', '');
    break;
  case ActionTypes.RECEIVE_REGISTER_RESPONSE:
    if (Store.getWaitingForRegisterFlag()) {
      Store.setWaitingForRegisterFlag(false);
      Store.setLastServerStatus(action.status, action.msg);
      Store.emitChange();
    }
    break;
  default:
    // do nothing
  }

});

module.exports = Store;