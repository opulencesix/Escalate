/*
 * Top level app to initialize the root React component.
 */
window.jQuery = require('jquery');
var $ = window.jQuery;
var dummyLoadBootstrap = require('bootstrap');
var App = require('./components/AppMain.react');
var AppWebAPIUtils = require('./common/AppWebAPIUtils');
var React = require('react');
window.React = React; // export for http://fb.me/react-devtools
var ReactDOM = require('react-dom');

AppWebAPIUtils.initApp();

//This is a specific event to be enabled to hide menus on small devices.
//See: http://stackoverflow.com/questions/21203111/bootstrap-3-collapsed-menu-doesnt-close-on-click
$(document).on('click','.navbar-collapse.in',function(e) {
  if( $(e.target).is('a') && $(e.target).attr('class') != 'dropdown-toggle' ) {
      $(this).collapse('hide');
  }
});


ReactDOM.render(
    <App />,
    document.getElementById('rootDiv')
);