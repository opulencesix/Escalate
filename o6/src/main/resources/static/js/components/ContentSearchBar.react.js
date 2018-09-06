/*
 * Content search bar, could be placed at various locations.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var UserViewActionCreators = require('../actions/UserViewActionCreators');
var Router = require('../common/Router');

module.exports = React.createClass({
  
  render: function() {
    return (
      <form onSubmit={this._onSearchClick}>
      <div className="input-group">
      <input type="text" className="form-control" ref="searchInput" 
        placeholder="Search.." />
      <span className="input-group-addon" onClick={this._onSearchClick}>
          <i className="fa fa-search" ></i>
      </span>
      </div></form>

      );
  },
    
  _onSearchClick: function(evt) {
    evt.preventDefault();
    evt.stopPropagation();

    var searchStr = ReactDOM.findDOMNode(this.refs.searchInput).value.trim();
    
    if(searchStr === '') {
      return;
    }
    

    UserViewActionCreators.searchContent(searchStr);
    Router.load("#searchContent");
  }
  
    
});
