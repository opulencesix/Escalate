/*
 * List of content, to be used for listing any set of content favorites,
 * watched, currently watching, etc.
 */
var React = require('react');
var ContentViewActionCreators = require('../../actions/ContentViewActionCreators');


/*
 * Search bar to filter list
 */
module.exports = React.createClass({
  handleChange: function() {
    this.props.onUserInput(
      this.refs.filterTextInput.value
    );
  },
  render: function() {
    return (
      <form>
        <div className="row"><div className="col-xs-4">
        <input
          type="text"
          className="form-control"
          placeholder="Filter..."
          value={this.props.filterText}
          ref="filterTextInput"
          onChange={this.handleChange}
        />
        </div></div>
      </form>
    );
  }
});

