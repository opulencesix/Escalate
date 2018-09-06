/*
 * This corresponds to the main section, the one containing dynamic content
 * based on the app option selected. Only route handler setting.
 */
var React = require('react');

var LoginReg = require('./LoginReg.react');

var InProgressContentList = require('./ContentListInProgress.react');
var FavoriteContentList = require('./ContentListFavorite.react');
var SpecialContentList = require('./ContentListSpecial.react');
var SearchContentList = require('./ContentListSearch.react');
var PremiumContentModules = require('./PremiumContentModules.react');

var StandaloneContentPlayer  = require('./players/StandaloneContentPlayer.react');
var UpcomingContentPlayer  = require('./players/UpcomingContentPlayer.react');
var SeenContentList = require('./ContentListSeen.react');
var ProfileModify = require('./ProfileModify.react');
var ChangePassword = require('./ChangePassword.react');
var NotificationSection = require('./SectionNotification.react');


var Router = require('../common/Router');


module.exports = React.createClass({  
  getInitialState: function() {
    return {
      page: null
    };
  },
  
  componentDidMount: function() {
    
    // Login or register
    Router.addRoute('loginReg', function() {
      this.setState({needNotification: false, page: <LoginReg />});
    }.bind(this));
    
    // Play content, either auto, or explicitly selected.
    Router.addRoute('autoplay', function() {
      this.setState({needNotification: true, page: <UpcomingContentPlayer />});
    }.bind(this));
    Router.addRoute('watch', function() {
      this.setState({needNotification: true, page: <StandaloneContentPlayer />});
    }.bind(this));
      
    // Content Listing components
    Router.addRoute('history', function() {
      this.setState({needNotification: true, page: <SeenContentList />});
    }.bind(this));
    Router.addRoute('inProgress', function() {
      this.setState({needNotification: true, page: <InProgressContentList />});
    }.bind(this));
    Router.addRoute('favorite', function() {
      this.setState({needNotification: true, page: <FavoriteContentList />});
    }.bind(this));
    Router.addRoute('special', function() {
      this.setState({needNotification: true, page: <SpecialContentList />});
    }.bind(this));
    Router.addRoute('searchContent', function() {
      this.setState({needNotification: true, page: <SearchContentList />});
    }.bind(this));
    
    // Profile update
    Router.addRoute('profileModify', function() {
      this.setState({needNotification: false, page: <ProfileModify />});
    }.bind(this));

    // Profile update
    Router.addRoute('changePassword', function() {
      this.setState({needNotification: false, page: <ChangePassword />});
    }.bind(this));
    
    // Premium Content modules
    Router.addRoute('premiumContentModules', function() {
      this.setState({needNotification: true, page: <PremiumContentModules />});
    }.bind(this));
      
    Router.start();
  },
  
  render: function() {
  
    // Show the main section in a two column layout if the notification panel is
    // needed
    if(this.state.needNotification) {
      return (
              <div className="row">
              <div className="col-sm-9">{this.state.page}</div>
              <div className="col-sm-3"><NotificationSection /></div>
              </div>
              
              );
    }
    
    return this.state.page;
  }
  
});
