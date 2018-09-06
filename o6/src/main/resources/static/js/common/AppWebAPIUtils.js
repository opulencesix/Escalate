/*
 * Web service calls and responses that in turn create server actions.
 */

var AppCommons = require('./AppCommons');
var Utils = require('./CommonUtils');
var AppServerActionCreators = require('../actions/AppServerActionCreators');
var UserViewActionCreators = require('../actions/UserViewActionCreators');

// Currently dummy web service responses are used. These will be
// replaced by actual web service success/failure handlers as
// those APIs are implemented.
var DummyWSResponses = require('./DummyWebServiceResults');

var $ = window.jQuery;
var assign = require('object-assign')

var baseUrl = window.location.origin + '/';

/*
 * Common Functions
 */
var getHeader = function() {
  var authInfo = Utils.retrieveAuth();
  if (authInfo) {
    var currTime = Utils.secondsSinceEpoch();
    if (currTime - authInfo.tokenFetchTime > AppCommons.TOKEN_REFRESH_SECS) {
      refreshToken(authInfo);
    }
    return {
      'X-AUTH-TOKEN': authInfo.token
    };
  }
  return {};
}

// Refresh token after an interval, lesser than
// the token validity time (say, 7 days vs 1 hour).
function refreshToken(oldAuthInfo) {
  var url = baseUrl + 'api/user/refreshToken';

  return $.ajax({
    url: url,
    type: 'GET',
    cache: false,
    headers: {
      'X-AUTH-TOKEN': oldAuthInfo.token
    }
  }).done(function(response) {
    var newAuthInfo = assign(oldAuthInfo, {
      token: response,
      tokenFetchTime: Utils.secondsSinceEpoch()
    });
    Utils.storeAuth(newAuthInfo);
    if (Utils.isAuthInPermStore()) {
      Utils.storeAuthPerm(newAuthInfo);
    }
  }).fail(function(request, textStatus, errorThrown) {
    UserViewActionCreators.logout();
    window.location.hash = "/#loginReg";
    console.error("Getting: ", url, textStatus, errorThrown);
  });
}

function getJson(params) {
  return $.ajax(assign(params, {
    type: 'GET',
    cache: false,
    dataType: 'json',
    headers: getHeader()
  }));
}

function postAjax(params) {
  return $.ajax(assign(params, {
    type: 'POST',
    headers: getHeader()
  }));
}

function postJson(params) {
  return $.ajax(assign(params, {
    type: 'POST',
    contentType: 'application/json; charset=utf-8',
    headers: getHeader()
  }));
}

function deleteJson(params) {
  return $.ajax(assign(params, {
    type: 'DELETE',
    contentType: 'application/json; charset=utf-8',
    headers: getHeader()
  }));
}

var moduleExports = {

  initApp: function() {
    this.requestNotifications();
    this.requestProfiles();
    this.requestProfileSchema();
    if (Utils.retrieveAuth()) {
      this.fetchPremiumContentModules();
    }
  },

  /*
   * Get listings for various types of content
   */
  getUpcomingContentForProfile: function(profile) {
    var url = baseUrl + 'api/content/newRelevant';
    postJson({
      url: url,
      data: JSON.stringify(profile)
    }).done(function(content) {
      AppServerActionCreators.receiveUpcomingContent(content);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      console.error("In posting: ", url, textStatus, errorThrown);
      AppServerActionCreators.receiveUpcomingContent([]);
    });
  },

  getConsumedContent: function() {
    this.getContentList('api/content/consumed',
            AppServerActionCreators.receiveConsumedContent);
  },

  getInProgressContent: function() {
    this.getContentList('api/content/inProgress',
            AppServerActionCreators.receiveInProgressContent);
  },

  getFavoriteContent: function() {
    this.getContentList('api/content/favorite',
            AppServerActionCreators.receiveFavoriteContent);
  },

  getSpecialContent: function() {
    this.getContentList('api/content/special',
            AppServerActionCreators.receiveSpecialContent);
  },

  searchContent: function(searchStr) {
    var url = baseUrl + 'api/content/search';
    postJson({
      url: url,
      data: JSON.stringify({
        searchStr: searchStr
      })
    }).done(function(content) {
      AppServerActionCreators.receiveSearchContent(content);
    }).fail(function(jqXHR, textStatus, errorThrown) {
      console.error("In posting: ", url, textStatus, errorThrown);
      AppServerActionCreators.receiveSearchContent([]);
    });
  },

  // Just ensure that getContentList is called via callers only directly
  // through the module.xxx call. E.g. module.getInProgressContent.
  getContentList: function(url, actionCreator) {
    var promise = getJson({
      url: url
    });

    return promise.then(function(data) {
      actionCreator(data);
    }, function(xhr, status, err) {
      console.error(url, status, err.toString());
    });
  },

  /*
   * Record events regarding content activities
   */
  recordContentEvent: function(id, language, eventType) {
    var url = baseUrl + 'api/content/event/' + eventType;
    return postJson({
      url: url,
      data: JSON.stringify({
        contentId: id,
        language: language
      })
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Posting: ", url, textStatus, errorThrown);
    });
  },

  recordConsumedAndPrefetch: function(id, language, profile) {
    this.recordContentEvent(id, language, 'consumed').always(function() {
      this.getUpcomingContentForProfile(profile);
    }.bind(this));
  },

  /*
   * Profile related
   */
  requestProfileSchema: function() {
    var url = 'api/profile/schema';
    var promise = getJson({
      url: url
    });

    return promise.done(function(response) {
      AppServerActionCreators.receiveProfileSchema(response);
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Getting: ", url, textStatus, errorThrown);
    });
  },

  requestProfiles: function() {
    var url = 'api/profile';
    var promise = getJson({
      url: url
    });

    return promise.done(function(response) {
      AppServerActionCreators.receiveProfiles(response);
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Getting: ", url, textStatus, errorThrown);
    });
  },

  saveProfile: function(profile) {
    var url = baseUrl + 'api/profile';
    if(!Utils.retrieveAuth()) {
      // Anon user profile need not be saved.
      return;
    }
    postJson({
      url: url,
      data: JSON.stringify(profile)
    }).fail(function(request, textStatus, errorThrown) {
      console.error(textStatus + errorThrown);
    });
  },

  deleteProfile: function(profileName, newCurrentName) {
    var url = baseUrl + 'api/profile?currentProfileName=' + profileName
            + '&newCurrentName=' + newCurrentName;
    if(!Utils.retrieveAuth()) {
      // Anon user profile need not be deleted.
      return;
    }
    deleteJson({
      url: url
    }).fail(function(request, textStatus, errorThrown) {
      console.error(textStatus + errorThrown);
    });
  },

  saveCurrentProfileName: function(name) {
    var url = baseUrl + 'api/profile/currentProfileName/' + name;
    if(!Utils.retrieveAuth()) {
      // Anon user profile need not be saved.
      return;
    }

    postJson({
      url: url,
    }).fail(function(request, textStatus, errorThrown) {
      console.error(textStatus + errorThrown);
    });
  },

  /*
   * Login and registration related
   */
  attemptRegister: function(regInfo) {
    var url = baseUrl + 'api/register';
    // TODO Handle errors better, pass message, etc.
    postJson({
      url: url,
      data: JSON.stringify({
        userName: regInfo.name,
        email: regInfo.email,
        password: regInfo.password,
        reCaptchaVal: regInfo.reCaptchaVal
      })
    }).done(function(user, textStatus, req) {
      Utils.storeAuth({
        name: regInfo.name,
        email: regInfo.email,
        token: req.getResponseHeader('X-AUTH-TOKEN'),
        tokenFetchTime: Utils.secondsSinceEpoch()
      });
      AppServerActionCreators.receiveRegisterResponse({
        name: regInfo.name,
        email: regInfo.email
      }, 'SUCCESS', textStatus);
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Registration: ", textStatus, errorThrown);
      AppServerActionCreators.receiveRegisterResponse({
        name: 'Anonymous',
        email: 'anonymous@opulencesix.com'
      }, 'FAILURE', request.responseText);
    });
    // Web service for register and login (below) have different signatures
    // so, error status is checked in different variables.
  },

  attemptLogin: function(loginInfo) {
    var url = baseUrl + 'api/login';
    postJson({
      url: url,
      data: JSON.stringify({
        email: loginInfo.email,
        password: loginInfo.password
      })
    }).done(function(user, textStatus, req) {
      var token = req.getResponseHeader('X-AUTH-TOKEN');
      var displayName = req.getResponseHeader('DISPLAY-USERNAME');
      Utils.storeAuth({
        name: displayName,
        email: loginInfo.email,
        token: token,
        tokenFetchTime: Utils.secondsSinceEpoch()
      });
      if (loginInfo.rememberMe) {
        Utils.storeAuthPerm({
          name: displayName,
          email: loginInfo.email,
          token: token,
          tokenFetchTime: Utils.secondsSinceEpoch()
        });
      }
      AppServerActionCreators.receiveRegisterResponse({
        name: displayName,
        email: loginInfo.email
      }, 'SUCCESS', textStatus);
    }).fail(function(request, textStatus, errorThrown) {
      console.log(textStatus + errorThrown);
      AppServerActionCreators.receiveRegisterResponse({
        name: 'Anonymous',
        email: 'anonymous@opulencesix.com'
      }, 'FAILURE', request.responseJSON.message);
    });
  },

  resetPassword: function(email) {
    var url = baseUrl + 'api/resetPassword';
    postJson({
      url: url,
      data: JSON.stringify({
        email: email
      })
    }).fail(function(request, textStatus, errorThrown) {
      console.log(url + ':' + email + ':' + textStatus + errorThrown);
    });

  },

  changePassword: function(oldPassword, newPassword) {
    var url = baseUrl + 'api/changePassword';
    postJson({
      url: url,
      data: JSON.stringify({
        password: oldPassword,
        newPassword: newPassword
      })
    }).fail(function(request, textStatus, errorThrown) {
      console.log(url + ':' + textStatus + errorThrown);
    });

  },

  /*
   * Notification related
   */
  requestNotifications: function() {
    var url = 'api/dynamicConfig/notifications';
    var promise = getJson({
      url: url
    });

    return promise.done(function(response) {
      AppServerActionCreators.receiveNotifications(response);
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Getting: ", url, textStatus, errorThrown);
    });
  },

  /*
   * Submit feedback.
   */
  submitFeedback: function(feedbackType, feedbackStr) {
    var url = baseUrl + 'api/feedback/' + feedbackType;
    postJson({
      url: url,
      data: feedbackStr
    }).fail(function(request, textStatus, errorThrown) {
      console.log(url + ':' + textStatus + errorThrown);
    });

  },

  /*
   * Premium content modules
   */
  fetchPremiumContentModules: function() {
    var url = 'api/contentModule';
    getJson({
      url: url
    }).done(function(response) {
      AppServerActionCreators.receiveContentModules(response);
    }).fail(function(request, textStatus, errorThrown) {
      console.error("Getting: ", url, textStatus, errorThrown);
    });

  },
  
  unlockForMe: function(contentModuleId, payGtwRespId) {
    var url = baseUrl + 'api/contentModule/unlockForMe?contentModuleId='
            + contentModuleId + "&paymentGatewayResponseId=" + payGtwRespId;
    postJson({
      url: url
    }).done(function(response) {
      var fetchContentModulesFunc = this.fetchPremiumContentModules;
      var fetchPremiumContentFunc = this.getSpecialContent.bind(this);
      refreshToken(Utils.retrieveAuth()).done(function() {
        fetchContentModulesFunc();
        fetchPremiumContentFunc();
        });
    }.bind(this)).fail(function(request, textStatus, errorThrown) {
      console.error(url + ':' + textStatus + errorThrown);
    });
  }

};

module.exports = moduleExports;
