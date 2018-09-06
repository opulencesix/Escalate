/*
 * Feedback widget, for logged in users. For complaints and feedback.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var UserViewActionCreators = require('../actions/UserViewActionCreators');

module.exports = React.createClass({
  render: function() {
    return (
      <div className="input-group">
        <textarea className="form-control noresize" rows="2" ref="feedbackTextArea" placeholder="Quick feedback or complaint..." />
        <div className="btn-group pull-right">
        <button className="btn btn-default btn-mini" onClick={this._onComplaint}><i className="fa fa-thumbs-o-down fa-fw" aria-hidden="true" title="Convey Unhappiness"></i></button>
        <button className="btn btn-default btn-mini" onClick={this._onFeedback}><i className="fa fa-comment-o fa-fw" aria-hidden="true" title="General Feedback"></i></button>
        </div>
      </div>

      );
  },
    
  _onComplaint: function(evt) {
    this._onSubmitFeedback("negative")
  },
  
  _onFeedback: function(evt) {
    this._onSubmitFeedback("positiveGeneral")
  },
  
  _onSubmitFeedback(feedbackType) {
    var textAreaNode = ReactDOM.findDOMNode(this.refs['feedbackTextArea']);
    var feedbackStr = textAreaNode.value.trim();
    
    if(feedbackStr === '') {
      return;
    }
    
    textAreaNode.value = "";
    UserViewActionCreators.submitFeedback(feedbackType, feedbackStr);
  }
  
    
});
