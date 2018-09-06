/*
 * This action creator is responsible for ensuring that  global
 * state stores of UI widgets that are dynamic, like modals, spinners, etc.
 * is set/reset appropriately if the component is unmounted
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Globals = require('../common/AppCommons');

var ActionTypes = Globals.ActionTypes;

module.exports = {

  // Unloading registration screen
  uiRegisterUnload: function() {
    AppDispatcher.handleViewAction({
      type: ActionTypes.UI_REGISTER_UNLOAD
    });
  },

};