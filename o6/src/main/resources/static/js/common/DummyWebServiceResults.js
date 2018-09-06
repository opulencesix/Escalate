/*
 * Sample Web service requests and responses.
 * User Id is automatically available in header token
 * Timestamp for event logging is implicit (server-side)
 * 
 */
var moduleExports = {

  /*
   * User Content listing related APIs
   * 
   */
  // POST api/content/newRelevant since get request has a body for filters, we need POST
  getNewRelevantContentRequestBody: {
    language: ["english"],
    profileCulture: ["all", "india"],
    profileAgeGroup: ["all", "adult"], // Any of
    profileTopics: ["technology"]
  },

  getNewRelevantContentResponse: [{
    contentId: "contentABC",
    title: "Content Title ABC",
    description: "Content description ABC",
    language: "english",
    authors: "ABC author1, ABC author2", // Or an array of authors, as is originally stored.
    mediaType: "youtubevideo",
    externalId: "XxVg_s8xAms",
    textUrl: "http://www.gutenberg.org/files/766/766-h/766-h.htm"
  },

  {
    contentId: "contentDEF",
    title: "Content Title DEF",
    description: "Content description DEF",
    language: "english",
    authors: "DEF author1, DEF author2",
    mediaType: "youtubevideo",
    externalId: "W9ae40yANYo",
    textUrl: "http://www.gutenberg.org/files/766/766-h/766-h.htm"
  }

  ],

  // GET api/content/inProgress, like getNewRelevantContentResponse
  getInProgressContentResponse: [],

  // GET api/content/consumed, like getNewRelevantContentResponse
  getConsumedContentResponse: [],

  // GET api/content/favorites, like getNewRelevantContentResponse
  getFavoritesContentResponse: [],

  // GET api/content/special, like getNewRelevantContentResponse
  getSpecialContentResponse: [],

  // GET api/content/search/{query}, like getNewRelevantContentResponse
  getSearchContentResponse: [],

  /*
   * Profile Related APIs
   */
  // GET api/profile/default. Available in a separate table, or config table
  getDefaultUserProfilesResponse: [{
    name: "O6-Generic",
    description: "Content applicable to all",
    language: ["english", "hindi"],
    profileCulture: ["all"],
    profileAgeGroup: ["all"],
    profileTopics: ["economics", "technology"]
  }, {
    name: "O6-Kids",
    description: "Content for kids",
    language: ["english", "hindi"],
    description: "For Kids",
    profileAgeGroup: ["all"]
  },

  ],

  // GET api/profile
  getUserProfilesResponse: {
    currentProfileName: "",
    profiles: [{
      name: "Me a Professional",
      description: "Related to professional life in general or India specific",
      language: ["english"],
      profileCulture: ["all", "india"],
      profileAgeGroup: ["all", "adult"], // Any of
      profileTopics: ["economics", "technology"]
    }

    ]
  },

  // PUT api/profile
  putProfileRequest: { // Insert or update if this profile name exists
    name: "Professional",
    description: "Related to professional life in general or India specific",
    language: ["english"],
    profileCulture: "all",
    profileAgeGroup: ["all", "adult"],
    profileTopics: ["economics", "technology"]
  },

  /*
   * 
   * Event related APIs
   * 
   */
  // POST api/content/event/{eventType}  e.g. skipped/inprogress/markFavorite/unmarkFavorite
  postRecordAppEventRequest: {
    contentId: "contentABC",
    language: "hindi"
  },

  /*
   * User login related APIs
   * 
   */
  // POST api/register
  postRegisterResponse: {
    userName: "User1",
    headerToken: "JUNK-XAUTH-TOKEN",
  },

  // POST api/register
  postLoginResponse: {
    userName: "User1",
    headerToken: "JUNK-XAUTH-TOKEN",
  },

  // POST api/resetPassword
  postResetPasswordRequest: {
    email: "abc@xyz.com"
  },
  
  // POST api/changePassword
  postResetPasswordRequest: {
    email: "abc@xyz.com",
    password: "junk1",
    newPassword: "junk2"
  }



};

// Overwrite similar variables
moduleExports.getInProgressContentResponse = moduleExports.getNewRelevantContentResponse;
moduleExports.getConsumedContentResponse = moduleExports.getNewRelevantContentResponse;
moduleExports.getFavoritesContentResponse = moduleExports.getNewRelevantContentResponse;
moduleExports.getSearchContentResponse = moduleExports.getNewRelevantContentResponse;

moduleExports.getUserProfilesResponse.profiles = moduleExports.getDefaultUserProfilesResponse
        .concat(moduleExports.getUserProfilesResponse.profiles);
moduleExports.getUserProfilesResponse.currentProfileName = moduleExports.getUserProfilesResponse.profiles[1].name;

module.exports = moduleExports;
