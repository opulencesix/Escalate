/*
 * List of content, to be used for listing any set of content favorites,
 * watched, currently watching, etc.
 */
var React = require('react');
var ContentViewActionCreators = require('../../actions/ContentViewActionCreators');
var SearchBar = require('./ListFilterBar.react');
var FilteredContentList = require('./FilteredList.react');
var MoreLessText = require('./MoreLessText.react');


module.exports = React.createClass({
  getInitialState: function() {
    return {
      filterText: ''
    };
  },
  
  handleUserInput: function(filterText) {
    this.setState({
      filterText: filterText
    });
  },
  
  render: function() {
    return (
    <div>
      <h2>{this.props.heading}</h2>
      <SearchBar filterText={this.state.filterText} onUserInput={this.handleUserInput}/>
      <hr/>
      <FilteredContentList displayList={this.props.contentList} 
        filterText={this.state.filterText} _shouldBeFilteredFunc={this._shouldBeFilteredFunc}
        displaySectionGenFunc={this._displaySectionGenFunc}
      />
    </div>);
  },
  
  _shouldBeFilteredFunc: function(content) {
    var filterTextLC = this.state.filterText.toLowerCase();
    if (content.title.toLowerCase().indexOf(filterTextLC) === -1 && 
      (content.description.toLowerCase().indexOf(filterTextLC) === -1)) {
      return true;
    }
    return false;
  },
  
  _displaySectionGenFunc: function(content) {
    var standAloneViewClicked = function(contentId) {
      ContentViewActionCreators.conveyStandAloneContentId(contentId);
      return true;
    };
    
    return (
      <div key={content.contentId}>
      <p><a href="/#watch" onClick={function() {return standAloneViewClicked(content.contentId);}}>
        <strong>{content.title}</strong></a></p>
      <p><strong>By:</strong> {content.authors}</p>
      <p className="text-muted"><MoreLessText text={content.description} /></p>
      
      <hr/>
      </div>);
  }
  
});
