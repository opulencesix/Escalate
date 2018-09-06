/*
 * Text input form with some validations. Currently used in the login
 * registration page.
 */
var React = require('react');
var ReactDOM = require('react-dom');

module.exports = React.createClass({
  getInitialState: function() {
    return {isValid : true};
  },
  
  render: function() {
    return(
      <div className={'form-group' + (this.state.isValid ? '' : ' has-error ')}>
      <span className={this.state.isValid ? 'hidden' : ''}> <sm> {this.props.helpStr} </sm> </span>
      <input type={this.props.type} className="form-control"  ref="myVal" 
        placeholder={this.props.placeholder} required={this.props.required}
        autoFocus={this.props.autoFocus} onChange={this._validate} />
      </div>
    );
  },
  
  compValue: function() {
    return ReactDOM.findDOMNode(this.refs.myVal).value.trim();    
  },
  
  _validate: function() {
    var inputVal = this.compValue();
    if(inputVal.length > 0 && !this.props.validateFunction(inputVal)) {
      this.setState({isValid: false});
    } else {
      this.setState({isValid: true});
    }
  }
});
