/*
 * Top level navbar and menus in it
 */
var React = require('react');
var UserStore = require('../stores/UserStore');
var ProfileStore = require('../stores/ProfileStore');
var UserViewActionCreators = require('../actions/UserViewActionCreators');
var Utils = require('../common/CommonUtils');

var getStateFromStores = function() {
  return {
    userName: UserStore.getUser().name,
    profileName: ProfileStore.getCurrentProfileName(),
    allProfileNames: ProfileStore.getAllUserProfileNames()
  };
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  // The User name and profiles in the Header Section depend upon those stores.
  componentDidMount: function() {
    UserStore.addChangeListener(this._onChange);
    ProfileStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    UserStore.removeChangeListener(this._onChange);
    ProfileStore.removeChangeListener(this._onChange);
  },
  
  render: function () {
    var anonUserFlag = ("Anonymous".valueOf() === this.state.userName.valueOf() ? true : false);
    // Login logout icons and text depending upon whether user has logged in or not.
    var liLoIcon = (anonUserFlag ? "fa-sign-in" : "fa-sign-out");
    var liLoText = (anonUserFlag ? "Sign In" : "Sign Out");
    
    return (
      <div>
      <nav className="navbar navbar-default navbar-fixed-top" role="navigation">
      	<div className="container">
          <div className="navbar-header">
            {/* The drawer like collapse button for small screens */}
            <button type="button" className="navbar-toggle collapsed" data-toggle="collapse" data-target=".navbar-collapse">
              <span className="sr-only">Toggle navigation</span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
              <span className="icon-bar"></span>
            </button>
            <a className='navbar-brand' href='#'><strong>O6Escalate</strong></a>
            
          </div>


          <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
          

            {/* Menu for various views of content, like a library */}
            <ul className="nav navbar-nav">
              <li className="dropdown">
              <a href="#" className="dropdown-toggle" data-toggle="dropdown" role="button" aria-expanded="false">Library <i className="fa fa-caret-down"></i></a>
              <ul className="dropdown-menu" role="menu">
              <li><a href="#autoplay">Auto Play</a></li>
              <li className="divider"></li>
              <li><a href="#special">My Premium Content</a></li>
              <li className="divider"></li>
              <li><a href="#favorite">My Favorites</a></li>
              <li><a href="#inProgress">In Progress</a></li>
              <li><a href="#history">History</a></li>
              </ul>
              </li>
            </ul>
            
            <ul className="nav navbar-nav navbar-right">
              {/* Profile select box */}
              <li>
              <select className="span2 navbar-btn form-control" onChange={this._onProfileChange} value={this.state.profileName} ref="profileSelect" id="sel1">
              {this.buildProfileNameOptions()}
              </select>
              </li>
              
              
              {/* User and profile manipulation */}
              <li className="dropdown">
              <a className="dropdown-toggle" data-toggle="dropdown" href="#">
              
              <i className="fa fa-user fa-fw"></i>  {Utils.toTitleCase(this.state.userName)} <i className="fa fa-caret-down"></i>
               </a>
              <ul className="dropdown-menu" role="menu">
                {this._getMyProfileAndChangePasswordMenus()}
                <li key="someLargeNum100"><a href="#loginReg" onClick={this._onLogoutClick}><i className= {"fa " + liLoIcon + " fa-fw"}></i> {liLoText} </a>
                </li>
              </ul>
            </li>
            
            </ul>            
          </div>
        </div>
      </nav>
      
      </div>
    );
  },
  
  _onProfileChange: function() {
    UserViewActionCreators.profileUpdate(ProfileStore.getProfileFromName(this.refs.profileSelect.value));
  },

  _onLogoutClick: function() {
    UserViewActionCreators.logout();
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  },
  
  _getMyProfileAndChangePasswordMenus: function() {
    var anonUserFlag = ("Anonymous".valueOf() === this.state.userName.valueOf() ? true : false);
    var retVal = [];
    retVal.push(<li key="1"> <a href="#profileModify" > <i className="fa fa-users fa-fw"></i> My Profiles</a></li>);

    if (!anonUserFlag) {
      retVal.push(<li key="2" className="divider"></li>);
      retVal.push(<li key="3"> <a href="#changePassword" > <i className="fa fa-key fa-fw"></i> Change Password </a></li>);
    }
    
    return retVal;
  },
  
  buildProfileNameOptions: function() {
    var optionList = [];
    for(var i=0; i<this.state.allProfileNames.length; i++) {
      var profileName = this.state.allProfileNames[i];
      optionList.push(<option key={profileName} title={ProfileStore.getProfileFromName(profileName).description} value={profileName}>Profile: {profileName}</option>);
    }
    
    return optionList;
  }

});

