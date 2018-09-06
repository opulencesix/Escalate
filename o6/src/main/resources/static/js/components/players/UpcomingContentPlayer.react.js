/*
 * Play upcoming content. Allows skipping, marking favorite. Move to next
 * upcoming content for the current user once current one is over.
 */
var React = require('react');
var ContentStore = require('../../stores/ContentStore');
var CommonContentPlayer = require('./CommonContentPlayer.react');
var ContentInfoPane = require('../common/ContentInfoPane.react');
var ContentViewActionCreators = require('../../actions/ContentViewActionCreators');

var getStateFromStores = function() {
  return {
    nextUpcomingId: ContentStore.getNextUpcomingIdAndMarkInProgress(),
    lastFetchStatus: ContentStore.getLastFetchStatus()
  };
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  componentDidMount: function() {
    ContentStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContentStore.removeChangeListener(this._onChange);
  },
  
  render: function() {
    
    // Null id means that either content is being fetched, or was not available.
    if(this.state.nextUpcomingId === null) {
//      if(this.state.lastFetchStatus !== "EMPTY") {
//      return (
//              <div> 
//                <h2> Auto Play</h2>
//                <div><i className="fa fa-spinner fa-spin fa-lg"></i></div>
//              </div>);
//      } else {
        return (
                <div> 
                  <h2> Auto Play</h2>
                  <p>No content yet available for selected user profile...</p>
                </div>);
        
//      }
    }
    
    var content = ContentStore.getContentDetails(this.state.nextUpcomingId);

    return (
      <div>
      <h2>Auto Play</h2>
      
      <ContentInfoPane content={content} onSkipped={this._onSkipped} />

      <CommonContentPlayer content={content} onEnd={this._onEnd} />
      </div>
      );
  },
  
  /*
   * Relay back events such as marked as favorite, view complete, etc.
   */
  _onChange: function() {
    var newState = getStateFromStores();
    if(!newState.nextUpcomingId || newState.nextUpcomingId !== this.state.nextUpcomingId) {
      this.setState(getStateFromStores());      
    }
  },
  
  _onSkipped: function() {
    ContentViewActionCreators.conveyUpcomingContentSeen(this.state.nextUpcomingId);
  },
  
  _onEnd: function() {
    console.log("Video ended");
    ContentViewActionCreators.conveyUpcomingContentSeen(this.state.nextUpcomingId);
  }
  
});
