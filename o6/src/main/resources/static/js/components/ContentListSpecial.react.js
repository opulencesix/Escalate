/*
 * Listing for special content.
 */

var React = require('react');
var ContentList = require('./common/ContentList.react');
var ContentStore = require('../stores/ContentStore');

var getStateFromStores = function() {
  return {
    contentList: ContentStore.getSpecialContent()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  render: function() {
    return (<ContentList heading="My Premium Content" contentList={this.state.contentList} />);
  }
});

