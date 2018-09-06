/*
 * App footer
 */
var React = require('react');

module.exports = React.createClass({
  render: function() {
    return(
      <footer className="footer">
        <div className="container">
        
        <span className="pull-left">
          &copy; 2016 <a href="http://www.opulencesix.com" target="_blank">OpulenceSix Digital Pvt. Ltd.</a>
        </span>
        
        <span className="pull-right">
        <a href="http://www.opulencesix.com/terms-of-service" target="_blank">Terms</a> |
        <a href="https://www.facebook.com/opulencesix" target="_blank"><i className="fa fa-facebook fa-fw"></i></a> 
        <a href="https://twitter.com/opulencesix" target="_blank"><i className="fa fa-twitter fa-fw"></i></a> 
        <a href="https://www.linkedin.com/company/opulencesix-digital-private-limited" target="_blank"><i className="fa fa-linkedin fa-fw"></i></a> 
        </span>
        
        </div>
      </footer>
    );
  }      
});