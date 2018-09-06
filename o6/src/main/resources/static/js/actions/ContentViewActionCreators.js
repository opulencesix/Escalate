/*
 * Action creators to relay actions from the content related views
 */
var AppDispatcher = require('../dispatcher/AppDispatcher');
var Globals = require('../common/AppCommons');

var ActionTypes = Globals.ActionTypes;

module.exports = {

  // For inprogress content
  conveyContentViewing: function(id) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.CONTENT_VIEW_STARTED,
      id: id
    });
  },

  conveyContentSeen: function(id) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.CONTENT_SEEN,
      id: id
    });
  },

  conveyUpcomingContentSeen: function(doneId) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.UPCOMING_CONTENT_SEEN,
      id: doneId
    });
  },

  conveyUpcomingContentSkipped: function(id) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.UPCOMING_CONTENT_SKIPPED,
      id: id
    });
  },

  conveyContentFavoriteToggled: function(id) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.CONTENT_FAVORITE_TOGGLED,
      id: id
    });
  },
  
  conveyStandAloneContentId: function(id) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.STAND_ALONE_CONTENT_ID_CHANGED,
      id: id
    });
  },
  
  contentModulePurchased(contentModuleId, paymentGatewayResponseId) {
    AppDispatcher.handleViewAction({
      type: ActionTypes.PURCHASE_CONTENT_MODULE,
      contentModuleId: contentModuleId,
      paymentGatewayResponseId: paymentGatewayResponseId
      
    });
  }

};