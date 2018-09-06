/*
 * User profile setup, login, registration etc.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var UserStore = require('../stores/UserStore');

var UserViewActionCreators = require('../actions/UserViewActionCreators');
var Utils = require('../common/CommonUtils');

var Router = require('../common/Router');
var assign = require('object-assign');

var getStateFromStores = function() {
  return {
    userName: UserStore.getUser().name,
  };
}

module.exports = React.createClass({
  getInitialState: function() {
    return assign({}, getStateFromStores(), {uiErrorMsg: ''});
  },
  
  render: function() {
    var buttonClassName = "btn btn-lg btn-primary btn-block"
    
    // Errors from UI if any.
    var uiErrorMsgDiv = <div />
    if(this.state.uiErrorMsg !== '') {
      uiErrorMsgDiv = <div className="alert alert-danger" role="alert"> {this.state.uiErrorMsg} </div>
    }
        
    return (
      <div className="row">
      <div className="col-md-5">
      <div className="form-signin center-block">
        <h2 className="form-signin-heading">Change password for <mute>{this.state.userName}</mute></h2>
        <p>After you submit password change request, you will be logged out, and will need to login again with new password.</p>
        {uiErrorMsgDiv}

        <form onSubmit={this._onChangePasswordClick}>
        <fieldset id="basicProfileForm" >
          <div className="form-group"><input type="password" ref="oldpassword" id="oldPassword" className="form-control" placeholder="Existing Password" required /></div>
          <div className="form-group"><input type="password" ref="newpassword" id="newPassword" className="form-control" placeholder="New Password" required /></div>
          <div className="form-group"><input type="password" ref="newpassword1" id="newPassword1" className="form-control" placeholder="Re-enter New Password" required /></div>          
          <button className={buttonClassName} type="submit"> Submit</button>        
        </fieldset>
        </form>
      </div>
      </div>
      </div>
      );
  },
    
  _onChangePasswordClick: function(evt) {
    evt.preventDefault();
    evt.stopPropagation();

    var errorMsg = '';
    
    var oldPasswd = ReactDOM.findDOMNode(this.refs.oldpassword).value;
    var newPasswd = ReactDOM.findDOMNode(this.refs.newpassword).value;
    var newPasswd1 = ReactDOM.findDOMNode(this.refs.newpassword1).value;
    if(oldPasswd === '' || newPasswd == '' || oldPasswd === newPasswd || newPasswd !== newPasswd1 || newPasswd.length < 6) {
      errorMsg += 'Passwords should have minimum length of 6, new passwords should match and be different from old one. ';
    }
    
    if(errorMsg === '') {
      UserViewActionCreators.changePassword(oldPasswd, newPasswd);
      Router.load("#loginReg");
    } else {
      this.setState(assign({}, getStateFromStores(), {uiErrorMsg: errorMsg}));
    }
  }
  
    
});
