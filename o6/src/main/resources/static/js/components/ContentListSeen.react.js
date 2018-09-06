/*
 * List content already seen.
 */
var React = require('react');
var ContentList = require('./common/ContentList.react');
var ContentStore = require('../stores/ContentStore');

var getStateFromStores = function() {
  return {
    contentList: ContentStore.getSeenContent()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  render: function() {
    return (<ContentList heading="Viewing Complete" contentList={this.state.contentList} />);
  }
});

