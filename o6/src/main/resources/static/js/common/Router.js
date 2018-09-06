/*
 * Simple router help in calling the appropriate component whenever the route
 * changes.
 */
var UserStore = require('../stores/UserStore');

var routes = [];
var redirectedFrom = '';

module.exports = (function() {

  // Callers call this to add handlers for individual routes
  function addRoute(route, handler) {

    routes.push({
      route: route,
      parts: route.split('/'),
      handler: handler
    });
  }

  // Go to, or retrieve current route
  function load(route) {
    window.location.hash = route;
  }

  function getCurrentRoute(route) {
    return window.location.hash;
  }

  // Load previously blocked route due to user not being registered previously.
  // Not very useful now, since we allow anonymous login.
  function continuePreviousLoad() {
    load(redirectedFrom);
  }

  // Manage special routing conditions, when a requested route should actually
  // redirect to some other route.
  function manageSpecialRoutingConditions() {
    // For anonymous users, only if they explicitly give a URL,
    // they are taken to it, else, it is assumed that they have forgotten
    // to logon and hence, asked to do so. Also, anonymous users are not shown
    // the profile and change password pages
    var currLocation = window.location.hash.substr(1);
    if (!UserStore.isAuthenticatedUser()
            && (currLocation === '' || currLocation === 'changePassword' )) {
      redirectedFrom = 'autoplay';
      load('loginReg');
    } else if (currLocation === '') {
      load('autoplay');
    }

  }

  // This function is called whenever the route changes
  // Run handler for matching route or redirect to default route
  function start() {

    manageSpecialRoutingConditions();
    var path = window.location.hash.substr(1), parts = path.split('/'), partsLength = parts.length;

    // Find registered route that matches pieces of the path, and call the
    // handler.
    for (var i = 0; i < routes.length; i++) {
      var route = routes[i];
      if (route.parts.length === partsLength) {
        var params = [];
        for (var j = 0; j < partsLength; j++) {

          if (route.parts[j].substr(0, 1) === ':') {
            params.push(parts[j]);
          } else if (route.parts[j] !== parts[j]) {
            break;
          }
        }
        // For this route, all parts have matched.
        if (j === partsLength) {
          route.handler.apply(undefined, params);
          return;
        }
      }
    }
    load('');
  }

  window.onhashchange = start;

  // Pack functions into object and export that as a module
  return {
    addRoute: addRoute,
    load: load,
    continuePreviousLoad: continuePreviousLoad,
    start: start
  };

}());
