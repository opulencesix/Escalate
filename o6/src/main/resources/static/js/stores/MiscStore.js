/*
 * This store is to store miscellaneous information such as
 * notifications
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Globals = require('../common/AppCommons');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

var _notifications = [];

var Store = assign({}, EventEmitter.prototype, {

  getNotifications: function() {
    return _notifications;
  },

  setNotifications: function(newNotifications) {
    _notifications = newNotifications
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

  case ActionTypes.RECEIVE_NOTIFICATIONS:
    Store.setNotifications(action.notifications);
    Store.emitChange();
    break;
  default:
    // do nothing
  }

});

module.exports = Store;