/*
 * Listing for special content.
 */

var React = require('react');
var ContentList = require('./common/ContentList.react');
var ContentStore = require('../stores/ContentStore');

var getStateFromStores = function() {
  return {
    contentList: ContentStore.getSearchContent()
  }
}

module.exports = React.createClass({
  getInitialState: function() {
    return getStateFromStores();
  },
  
  // The User name and profiles in the Header Section depend upon those stores.
  componentDidMount: function() {
    ContentStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContentStore.removeChangeListener(this._onChange);
  },
  

  
  render: function() {
    return (<ContentList heading="Search Results" contentList={this.state.contentList} />);
  },
  
  _onChange: function() {
    this.setState(getStateFromStores());
  }

});

