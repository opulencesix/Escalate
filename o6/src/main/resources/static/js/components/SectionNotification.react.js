/*
 * Notifications on the right hand side panel. TODO: Hardcoded for now, but will
 * move these to the server side. Have dynamic notifications.
 */
var React = require('react');
var ContentSearchBar = require('./ContentSearchBar.react');
var FeedbackWidget = require('./FeedbackWidget.react');
var UserStore = require('../stores/UserStore');
var MiscStore = require('../stores/MiscStore');

var getStateFromStores = function() {
  return {
    userName: UserStore.getUser().name,
    notifications: MiscStore.getNotifications()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  componentDidMount: function() {
    MiscStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    MiscStore.removeChangeListener(this._onChange);
  },
  
  _onChange: function() {
    this.setState(getStateFromStores());
  },
  
  render: function() {
    return (
      <div className="panel panel-primary">
      <div className="panel-heading panel-title">Quick Refs:
      </div>
      
      <ul className="list-group">
        <li key="0" className="list-group-item">
          <ContentSearchBar />
        </li>
        {"Anonymous".valueOf() === this.state.userName.valueOf() ? "" : 
          <li key="1" className="list-group-item">
            <FeedbackWidget />
          </li>
        }
        {"Anonymous".valueOf() === this.state.userName.valueOf() ? "" :
        <li key="2" className="list-group-item">
          <a href="/#premiumContentModules">Premium Content Modules</a>
        </li>
        }
        {this._getNotificationSection(3)}
      </ul>
      </div>
    );
  },
  
  _getNotificationSection: function(offset) {
    
    var notificationSections = [];
    if(this.state.notifications && this.state.notifications.length>0) {
      notificationSections.push(this.state.notifications.map(function(noti, index) {
        return (<li key={'"' + index+offset + '"'} className="list-group-item" dangerouslySetInnerHTML={{__html: noti}}></li>);
      }));
    }

    return notificationSections;
  }
  
    
});
