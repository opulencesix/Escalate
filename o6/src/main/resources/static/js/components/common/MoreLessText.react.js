/*
 * More or less text span component
 */
var React = require('react');

module.exports = React.createClass({
  getInitialState: function() {
    return {
      fullTextFlag : false
    };
  },
  
  render: function() {
    return (
      <span>{this._buildText()} 
        <a onClick={this._toggleExpanded}>{this._getReadMoreLessLinkText()}</a>
      </span> );
  },
  
  _buildText: function() {
    if(this.state.fullTextFlag) {
      return this.props.text;
    } else {
      return this.props.text.substring(0, 100) + '...';
    }
  },
  
  _getReadMoreLessLinkText: function() {
    return this.state.fullTextFlag ? " Less " : " More ";
  },
  
  _toggleExpanded: function() {
    this.setState({fullTextFlag: !this.state.fullTextFlag});
  },
  

});

