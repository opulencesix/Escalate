/*
 * Flux based dispatcher. Primarily dispatches only two types of actions server
 * and view.
 * 
 * TODO: Move to Redux from flux sometime
 */
var Dispatcher = require('flux').Dispatcher;
var assign = require('object-assign');
var Globals = require('../common/AppCommons');

var PayloadSources = Globals.PayloadSources;

var AppDispatcher = assign(new Dispatcher(), {  
  handleServerAction(action) {
    var payload = {
      source: PayloadSources.SERVER_ACTION,
      action: action
    };
    this.dispatch(payload);
  },
  handleViewAction(action) {
    var payload = {
      source: PayloadSources.VIEW_ACTION,
      action: action
    };
    this.dispatch(payload);
  }
});

module.exports = AppDispatcher;  