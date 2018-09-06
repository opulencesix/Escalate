/*
 * Used to play single standalone content only, not upcoming content
 */
var React = require('react');
var ContentStore = require('../../stores/ContentStore');
var CommonContentPlayer = require('./CommonContentPlayer.react');
var ContentInfoPane = require('../common/ContentInfoPane.react');
var ContentViewActionCreators = require('../../actions/ContentViewActionCreators');

module.exports = React.createClass({
  getInitialState: function() {
    return { contentId: ContentStore.getStandAloneContentId()};
  },
  
  componentDidMount: function() {
    if(this.state.contentId) {
      ContentViewActionCreators.conveyContentViewing(this.state.contentId);
    }
    ContentStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContentStore.removeChangeListener(this._onChange);
  },  
  
  _onChange: function() {
    var newId = ContentStore.getStandAloneContentId();
    if(newId && newId != this.state.contentId) {
      this.setState({ contentId: newId });      
    }
  },
  
  render: function() {
    var content = ContentStore.getContentDetails(this.state.contentId);
    if(!content) {
      return <div> No content selected for standalone play. Go to one of the content lists and select. </div>;
    }
    
    return (
      <div>
        <h2> Playing Selected Content </h2>
        
        <ContentInfoPane content={content} />

        <CommonContentPlayer content={content} onEnd={this._onEnd} />
      </div>
    );
  },
  
  _onEnd: function() {
    ContentViewActionCreators.conveyContentSeen(this.state.contentId);
  }
  
});

