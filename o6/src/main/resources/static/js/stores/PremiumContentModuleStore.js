/*
 * This store is to store and update premium content module information.
 */
var UserStore = require('./UserStore');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var AppWebAPIUtils = require('../common/AppWebAPIUtils');
var Globals = require('../common/AppCommons');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

var _unlockedContentModules = [];
var _lockedContentModules = [];

var Store = assign({}, EventEmitter.prototype, {
  clear: function() {
    _unlockedContentModules = [];
    _lockedContentModules = [];
  },

  getUnlockedContentModules: function() {
    return _unlockedContentModules;
  },

  getLockedContentModules: function() {
    return _lockedContentModules;
  },
  
  setUnlockedContentModules: function(newUnlockedModules) {
    _unlockedContentModules = newUnlockedModules;
  },

  setLockedContentModules: function(newLockedModules) {
    _lockedContentModules = newLockedModules;
  },
  
  getLockedModuleInfo: function(moduleId) {
    var lookup = {};
    for (var i = 0, len = _lockedContentModules.length; i < len; i++) {
      if(_lockedContentModules[i].id === moduleId) {
        return _lockedContentModules[i];
      }
    }
    return null;
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

  var action = payload.action;
  switch (action.type) {

  case ActionTypes.RECEIVE_CONTENT_MODULE_INFO:
    Store.setUnlockedContentModules(action.subscribedModules);
    Store.setLockedContentModules(action.unSubscribedModules);
    Store.emitChange();
    break;
  case ActionTypes.PURCHASE_CONTENT_MODULE:
    AppWebAPIUtils.unlockForMe(action.contentModuleId, action.paymentGatewayResponseId);
    Store.emitChange();
    break;
  case ActionTypes.LOGOUT:
    Store.clear();
    break;
  case ActionTypes.RECEIVE_REGISTER_RESPONSE:
    AppDispatcher.waitFor([UserStore.dispatchToken]);
    if (action.status === 'SUCCESS') {
      AppWebAPIUtils.fetchPremiumContentModules();
    }
    break;
  default:
    // do nothing
  }

});

module.exports = Store;