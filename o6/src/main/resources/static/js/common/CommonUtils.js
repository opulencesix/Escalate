/*
 * Common utility functions
 */

var authKey = 'auth_key';

var moduleExports = {
  // Is email valid?
  isEmailValid: function(email) {
    var atpos = email.indexOf("@");
    var dotpos = email.lastIndexOf(".");
    if (atpos<1 || dotpos<atpos+2 || dotpos+2>=email.length) {
        return false;
    }
    return true;
  },
  
  /*
   * Related to Local and session storage for login related information.
   */
  storeAuth: function(userToken) {
    sessionStorage.setItem(authKey, JSON.stringify(userToken));
  },
  retrieveAuth: function() {
    return JSON.parse(sessionStorage.getItem(authKey));
  },
  clearAuth: function() {
    sessionStorage.removeItem(authKey);
    localStorage.removeItem(authKey);
  },
  storeAuthPerm: function(userToken) {
    localStorage.setItem(authKey, JSON.stringify(userToken));
  },
  retrieveAuthPerm: function() {
    return JSON.parse(localStorage.getItem(authKey));
  },
  
  isAuthInPermStore: function() {
    return localStorage.getItem(authKey) ? true : false;
  },

  // Convert abc def to Abc Def
  toTitleCase: function(str) {
    return str.replace(/\w\S*/g, function(txt) {
      return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
    });
  },

  // Return object values satisfying filter criteria in filter function.
  filterObjectVals: function(obj, filterFunc) {
    var ret = [];
    for ( var key in obj) {
      if (filterFunc(obj[key])) {
        ret.push(obj[key]);
      }
    }
    return ret;
  },
  
  // Get time since epoch in seconds
  secondsSinceEpoch : function() { 
    return Math.floor( Date.now() / 1000 ); 
  },

  /*
   * Currently unused functions, since profile to content profile match is done
   * on server side.
   * 
   * And of conditions for keys, within key, or of values for collections.
   */
  _unusedFilterProfileMatch: function(filterProfile, profile) {
    for ( var attribKey in filterProfile) {
      if (profile[attribKey]) {
        if (typeof filterProfile[attribKey] === 'object') {
          if (!this._haveCommon(filterProfile[attribKey], profile[attribKey])) { return false; }
        } else if (filterProfile[attribKey] !== profile[attribKey]) { return false; }
      }
    }
    return true;
  },
  _haveCommon: function(arr1, arr2) {
    for (var i = 0; i < arr1.length; i++) {
      if (arr2.indexOf(arr1[i]) != -1) { return true; }
    }
    return false;
  }
};

module.exports = moduleExports;
