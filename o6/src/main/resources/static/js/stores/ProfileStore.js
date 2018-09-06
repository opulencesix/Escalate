/*
 * Centralized management of profile information.
 */
var Globals = require('../common/AppCommons');
var AppWebAPIUtils = require('../common/AppWebAPIUtils');
var AppDispatcher = require('../dispatcher/AppDispatcher');
var EventEmitter = require('events').EventEmitter;
var assign = require('object-assign');

var ActionTypes = Globals.ActionTypes;
var CHANGE_EVENT = 'change';

var _profileSchema = null;

var _currentProfile = null;
var _allProfiles = Object.create(null);

var ProfileStore = assign({}, EventEmitter.prototype, {
  initProfileSchema: function(profileSchema) {
    _profileSchema = profileSchema;
  },
  
  initProfiles: function(profilesDTO) {
    _allProfiles = Object.create(null);
    var profiles = profilesDTO.allProfiles;
    for (var i = 0; i < profiles.length; i++) {
      _allProfiles[profiles[i].name] = profiles[i];
      if(i==0) {
        this.setProfile(profiles[0]);        
      }
      if(profilesDTO.currentProfileName === profiles[i].name) {
        this.setProfile(profiles[i]);
      }
    }
  },

  requestProfiles: function() {
    AppWebAPIUtils.requestProfileSchema();
    AppWebAPIUtils.requestProfiles();
  },

  /*
   * Getters for various profile related data.
   */
  getProfileFromName: function(profileName) {
    return _allProfiles[profileName];
  },

  getCurrentProfile: function() {
    return _currentProfile;
  },

  getDefaultProfile: function() {
    return {
      name: "New Profile",
      description: "General profile",
      language: ["english", "hindi"]
    };
  },

  getCurrentProfileName: function() {
    return _currentProfile ? _currentProfile.name : "";
  },

  getAllUserProfileNames: function() {
    var profileNames = [];
    for (var key in _allProfiles) {
      profileNames.push(key);
    }
    return profileNames;
  },

  /*
   * Set, delete and save profiles
   */
  setProfile: function(profile) {
    _currentProfile = profile;
    _allProfiles[profile.name] = profile;
  },

  deleteProfile: function(profileName) {
    delete _allProfiles[profileName];
    if(_currentProfile.name === profileName) {
      _currentProfile = _allProfiles[Object.keys(_allProfiles)[0]];
    }
    
    AppWebAPIUtils.deleteProfile(profileName, _currentProfile.name);
  },

  saveProfileAndSetIfCurrentModified: function(profile) {
    if(_currentProfile.name == profile.name) {
      _currentProfile = profile;      
    }
    _allProfiles[profile.name] = profile;
    AppWebAPIUtils.saveProfile(profile);
  },
  
  saveCurrentProfileName: function(name) {
    AppWebAPIUtils.saveCurrentProfileName(name);
  },

  getSchema: function() {
    return _profileSchema;
  },

  setSchema: function(schema) {
    _profileSchema = schema;
  },

  emitChange: function() {
    this.emit(CHANGE_EVENT);
  },

  addChangeListener: function(callback) {
    this.on(CHANGE_EVENT, callback);
  },

  removeChangeListener: function(callback) {
    this.removeListener(CHANGE_EVENT, callback);
  }

});

ProfileStore.dispatchToken = AppDispatcher.register(function(payload) {

  var action = payload.action;
  switch (action.type) {
  case ActionTypes.PROFILE_MODIFY:
    ProfileStore.saveProfileAndSetIfCurrentModified(action.profile);
    ProfileStore.emitChange();
    break;
  case ActionTypes.PROFILE_UPDATE:
    ProfileStore.setProfile(action.profile);
    ProfileStore.saveCurrentProfileName(action.profile.name);
    ProfileStore.emitChange();
    break;
  case ActionTypes.PROFILE_DELETE:
    ProfileStore.deleteProfile(action.profileName);
    ProfileStore.emitChange();
    break;
  case ActionTypes.LOGOUT:
  case ActionTypes.RECEIVE_REGISTER_RESPONSE:
    ProfileStore.requestProfiles();
    break;
  case ActionTypes.RECEIVE_PROFILES:
    ProfileStore.initProfiles(action.profiles);
    ProfileStore.emitChange();
    break;
  case ActionTypes.RECEIVE_PROFILE_SCHEMA:
    ProfileStore.initProfileSchema(action.profileSchema);
    ProfileStore.emitChange();
    break;
  default:
    // do nothing
  }

});

module.exports = ProfileStore;