/*
 * User profile setup, login, registration etc.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var UIStateStore = require('../stores/GlobalUIStateStore');

var Validator = require('validator');
var UserViewActionCreators = require('../actions/UserViewActionCreators');
var UIStateActionCreators = require('../actions/UIStateActionCreators');
var FormTextInput = require('./common/FormTextInput.react');
var HelpCarousel = require('./HelpCarousel.react');
var Utils = require('../common/CommonUtils');
var Globals = require('../common/AppCommons');

var $ = window.jQuery;

var Router = require('../common/Router');
var assign = require('object-assign');
var _componentUnloadedOnce = false;

var getStateFromStores = function() {
  return {
    waitingForRegisterFlag: UIStateStore.getWaitingForRegisterFlag(),
    lastAuthServerResponse: UIStateStore.getLastServerStatus(),
    lastSrvMsg: UIStateStore.getSrvMsg()
  };
}


module.exports = React.createClass({
  getInitialState: function() {
    return assign({}, getStateFromStores(), {uiErrorMsg: '', uiInfoMsg: ''});
  },
  
  componentDidMount: function() {
    UIStateStore.addChangeListener(this._onChange);
    if(typeof(grecaptcha) != 'undefined' && _componentUnloadedOnce) {
      window.registerCaptchaWidget = grecaptcha.render('regCaptcha', { 'sitekey': Globals.AppConfig.RE_CAPTCHA_SITE_KEY, 'theme': 'light'});
    }
  },

  componentWillUnmount: function() {
    UIStateActionCreators.uiRegisterUnload();
    UIStateStore.removeChangeListener(this._onChange);
    _componentUnloadedOnce = true;
  },
  
  render: function() {
    if(this.state.lastAuthServerResponse === 'SUCCESS') {
      Router.continuePreviousLoad();
    }
    
    // TODO The Register flag is being overloaded to be used for
    // registration as well as login. Separate later.
    var spinnerIconClassName = "fa fa-circle-o-notch fa-spin";
    var buttonClassName = "btn btn-lg btn-primary btn-block"
    if(this.state.waitingForRegisterFlag) {
      buttonClassName += " disabled";
    } else {
      spinnerIconClassName += " hidden";
    }
    
    // Alerts from the server if any.
    var alertDiv = <div />
    if(this.state.lastAuthServerResponse === 'FAILURE') {
      alertDiv = <div className="alert alert-danger" role="alert"> {this.state.lastSrvMsg} </div>
    }
    
    // Errors from UI if any.
    var uiErrorMsgDiv = <div />
    if(this.state.uiErrorMsg !== '') {
      uiErrorMsgDiv = <div className="alert alert-danger" role="alert"> {this.state.uiErrorMsg} </div>
    }
    
    // Info from UI if any.
    var uiInfoMsgDiv = <div />
    if(this.state.uiInfoMsg !== '') {
      uiInfoMsgDiv = <div className="alert alert-success" role="alert"> {this.state.uiInfoMsg} </div>
    }
    
    return (
       
      <div className="row">
      <div className="col-md-5">
      <div className="form-signin center-block">
        {alertDiv}
        {uiErrorMsgDiv}
        {uiInfoMsgDiv}
        <ul className="nav nav-tabs form-tabs">
          <li className="active" id="login_details-list"> <a data-toggle="tab" href="#loginForm">Sign In</a></li>
          <li className="" id="basic_profile-list"><a data-toggle="tab" href="#basicProfileForm">Register</a></li>
        </ul>
        <div className="tab-content">

          {/* Login tab */}
          <form id="loginForm" className="tab-pane active" onSubmit={this._onLoginClick}>
          <fieldset >
            <h2 className="form-signin-heading">Please Sign In</h2>
            
            {/*
               * Disable Social login for now, add later <small
               * className="text-muted">Connect using your favorite social
               * network</small> <br/><br/>
               * 
               * <div className="form-group"> <button className="socialLoginBtn
               * loginBtn--facebook"> Facebook </button>
               * 
               * <button className="socialLoginBtn loginBtn--google"> Google
               * </button> </div>
               * 
               * <small className="text-muted">Or sign in with </small> <br/><br />
               */}

            <label htmlFor="inputEmail" className="sr-only">Email</label>
            <div className="form-group"><input type="email" ref="loginemail" className="form-control" placeholder="Email.." required autoFocus /></div>
            <label htmlFor="inputPassword" className="sr-only">Password</label>
            <div className="form-group"><input type="password" ref="loginpassword" id="inputPassword" className="form-control" placeholder="Password" required /></div>
            <div className="checkbox">
              <label>
                <input type="checkbox" ref="rememberMe" /> Remember me
              </label>
            </div>
            <p><a href="#autoplay">Continue Anonymously with limited functionality</a></p>
            <button className={buttonClassName} type="submit" > <i className={spinnerIconClassName}></i> Sign In</button>        
            <p><br/><a href="#" onClick={this._onForgotPasswordClick}>Forgot Password? Enter email above and click here</a></p>
          </fieldset>
          </form>

          {/* The new user registration tab */}
          <form id="basicProfileForm" className="tab-pane" onSubmit={this._onRegisterClick}>
          <fieldset>
            <h2 className="form-signin-heading">Please Register</h2>
            <FormTextInput helpStr="Only alphanumerics in username"
              type="text" ref="regusername" placeholder="Username.." 
              required="required" autoFocus="autofocus"
              validateFunction={Validator.isAlphanumeric}            
            />
            <div className="form-group"><input type="email" ref="regemail" id="inputEmail" className="form-control" placeholder="Email.." required autoFocus /></div>
            <div className="form-group"><input type="password" ref="regpassword" id="inputPassword" className="form-control" placeholder="Password" required /></div>
            <div className="form-group"><input type="password" ref="regpassword1" id="inputPassword1" className="form-control" placeholder="Re-enter Password" required /></div>
            
            <div className="checkbox">
            <label>
              <input type="checkbox" ref="acceptTerms" /> Accept <a target='_blank' href='http://www.opulencesix.com/terms-of-service'>terms </a>
                , <a target='_blank' href='http://www.opulencesix.com/delivery-and-shipping-policy/'> delivery </a>
                and <a target='_blank' href='http://www.opulencesix.com/refund-and-cancellations/'> refund  </a> policies?
            </label>
            </div>
            
            <div id="regCaptcha" className="form-control-margin-bottom"></div>

            <button className={buttonClassName} type="submit"> <i className={spinnerIconClassName}></i> Submit</button>        
          </fieldset>
          </form>
      
        </div>
      </div>
      </div>
      <div className="col-md-6 col-md-offset-1">
      
      <HelpCarousel />
      
      </div>
      </div>
      );
  },
  
  _onChange: function() {
    this.setState(getStateFromStores());
  },
  
  _onRegisterClick: function(evt) {
    
    var formData = $('#basicProfileForm').serializeArray().reduce(function(obj, item) {
      obj[item.name] = item.value;
      return obj;
    }, {});
    var reCaptchaVal = formData['g-recaptcha-response'];
    
    evt.preventDefault();
    evt.stopPropagation();

    var errorMsg = '';
    var userName = this.refs.regusername.compValue().trim();
    if(userName == '') {
      errorMsg += 'Please specify user name. ';
    }
    if(userName === "Anonymous") {
      errorMsg += 'User name Anonymous is reserved. Please specify a different one". ';
    }
    if(!Utils.isEmailValid(ReactDOM.findDOMNode(this.refs.regemail).value.trim())) {
      errorMsg += 'Please specify valid email. ';
    }
    var passwd = ReactDOM.findDOMNode(this.refs.regpassword).value;
    if(passwd.length < 6 || passwd !== ReactDOM.findDOMNode(this.refs.regpassword1).value.trim()) {
      errorMsg += 'Password should be at least 6 characters, and should match. ';
    }
    if(!ReactDOM.findDOMNode(this.refs.acceptTerms).checked) {
      errorMsg += 'First accept terms and conditions. ';
    }
    if(!reCaptchaVal || reCaptchaVal == '') {
      errorMsg += 'Captcha unsolved. ';
    }
    
    if(errorMsg === '') {
      UserViewActionCreators.attemptToRegister({name: this.refs.regusername.compValue(),
                                                email: ReactDOM.findDOMNode(this.refs.regemail).value.trim(),
                                                password: passwd, reCaptchaVal: reCaptchaVal})
    } else {
      this.setState(assign({}, getStateFromStores(), {uiErrorMsg: errorMsg, uiInfoMsg: ''}));
    }
  },
  
  _onForgotPasswordClick: function() {
    var errorMsg = '';
    var infoMsg = '';
    
    if(!Utils.isEmailValid(ReactDOM.findDOMNode(this.refs.loginemail).value.trim())) {
      errorMsg += 'Please specify valid email. ';
    } else {
      infoMsg += 'Password reset requested. We will email you the new password in some time. ';
    }
    
    if(errorMsg !== '' || infoMsg !== '') {
      this.setState(assign({}, getStateFromStores(), {uiErrorMsg: errorMsg, uiInfoMsg: infoMsg}));      
    }

    UserViewActionCreators.resetPassword(ReactDOM.findDOMNode(this.refs.loginemail).value.trim());
    
    // Return false always so that we stay on same page
    return false;
  },

  _onLoginClick: function(evt) {
    evt.preventDefault();
    evt.stopPropagation();

    UserViewActionCreators.attemptToLogin({email: ReactDOM.findDOMNode(this.refs.loginemail).value.trim(), 
                                              password: ReactDOM.findDOMNode(this.refs.loginpassword).value.trim(), 
                                              rememberMe: ReactDOM.findDOMNode(this.refs.rememberMe).checked })
  }
    
});
