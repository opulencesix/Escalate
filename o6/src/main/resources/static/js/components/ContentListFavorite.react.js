/*
 * Listing for favorites content.
 */

var React = require('react');
var ContentList = require('./common/ContentList.react');
var ContentStore = require('../stores/ContentStore');

var getStateFromStores = function() {
  return {
    contentList: ContentStore.getFavoriteContent()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  render: function() {
    return (<ContentList heading="Favorites" contentList={this.state.contentList} />);
  }
});

