/*
 * Listing for in progress content.
 */


var React = require('react');
var ContentList = require('./common/ContentList.react');
var ContentStore = require('../stores/ContentStore');
var getStateFromStores = function() {
  return {
    contentList: ContentStore.getInProgressContent()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  render: function() {
    return (<ContentList heading="Partially Viewed" contentList={this.state.contentList} />);
  }
});

