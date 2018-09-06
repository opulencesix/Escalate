/*
 * List of content, to be used for listing any set of content favorites,
 * watched, currently watching, etc.
 */
var React = require('react');


/*
 * Content list filtered by filter string in the filterbox.
 */
module.exports = React.createClass({
  render: function() {
    var rows = [];
    this.props.displayList.forEach(function(content) {
      if (this.props._shouldBeFilteredFunc(content)) {
        return;
      }
      rows.push(this.props.displaySectionGenFunc(content));
    }.bind(this));
    
    var retVal = (<div>{rows}</div>);
    if(rows.length==0) {
      retVal = (<div><p> No items to display </p></div>);
    }
    
    return retVal;
    
  }
  
});


