/*
 * User profile update or creation of new profile.
 * 
 */
var React = require('react');
var ReactDOM = require('react-dom');
var ProfileStore = require('../stores/ProfileStore');
var Validator = require('validator');
var UserViewActionCreators = require('../actions/UserViewActionCreators');
var GenericFormInput = require('./common/GenericFormInput.react');
var Router = require('../common/Router');

var buildState = function(alertStr) {
  var profileToDisplay = ProfileStore.getCurrentProfile();
  profileToDisplay = profileToDisplay ? profileToDisplay : ProfileStore.getDefaultProfile();
  return {
    schema: ProfileStore.getSchema(),
    allProfileNames: ProfileStore.getAllUserProfileNames(),
    profile: profileToDisplay,
    alertStr: alertStr
  };
}

module.exports = React.createClass({
  getInitialState: function() {
    return buildState();
  },
  
  componentDidMount: function() {
    ProfileStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ProfileStore.removeChangeListener(this._onChange);
  },
  
  componentDidUpdate() {
    window.scrollTo(0, 0);
  },
  
  _onChange: function() {
    this.setState(buildState());
  },
  
  render: function() {
    
    if(this.state.schema) {
    return (
      <div>
        <h3>Update Profile</h3>

        {this._getAlertSectionIfSet()}
        <GenericFormInput schema={this.state.schema} currentValue={this.state.profile} 
          buttonName="Save" onClick={this._onProfileModify} 
          onDeleteClick={this._onProfileDelete} onCancelClick={this._onCancel} 
        ref='profileForm' />
        
      </div>
    );
    } else {
      return (
              <div> 
                <h2> Update Profile </h2>
                <div><i className="fa fa-spinner fa-spin fa-lg"></i>...ProfileSchema not available. Try later. </div>
              </div>);
    }
  },
  
  _getAlertSectionIfSet: function() {
    if(!this.state.alertStr) {
      return (<div/>)
    }
    
    return (
      <div className="alert alert-danger alert-dismissible" role="alert">
      <button type="button" onClick={this._onAlertDismiss} className="close" aria-label="Close">
          <span aria-hidden="true">&times;</span></button>
      <strong>Error!</strong> {this.state.alertStr}
      </div>);

  },
  
  _onAlertDismiss: function() {
    this.setState(buildState());
  },
  
  _onProfileModify: function(evt) {
    //evt.preventDefault();
    //evt.stopPropagation();
    var fieldData = this.refs.profileForm.getFieldData();
    if(fieldData.name == '' || fieldData.name.substring(0,2) == "O6") {
      this.setState(buildState("User defined profile name should be non-empty and not begin with 'O6'. O6 profiles are reserved and unmodifiable."))
    } else {
      UserViewActionCreators.profileModify(fieldData);
      Router.load('#autoplay');
    }
  },
  
  _onProfileDelete: function(evt) {
    var fieldData = this.refs.profileForm.getFieldData();
    if(fieldData.name == '' || fieldData.name.substring(0,2) == "O6") {
      this.setState(buildState("O6 profiles are reserved and unmodifiable, cannot delete."))
    } else if(this.state.allProfileNames.indexOf(fieldData.name) == -1) {
      this.setState(buildState("Profile " + fieldData.name + " not existent, cannot delete."))
    } else {
      UserViewActionCreators.profileDelete(fieldData.name);
      Router.load('#autoplay');
    }
  }, 
  
  _onCancel: function(evt) {
    Router.load('#autoplay');
  }


    
});
