/*
 * Initialize the appropriate player based on current content type.
 */
var React = require('react');
var ContentStore = require('../../stores/ContentStore');
var YoutubePlayerAdapter = require('./YoutubePlayerAdapter.react');
var Globals = require('../../common/AppCommons');

module.exports = React.createClass({
  render: function() {
    var currentContent = this.props.content;
    var onEnd = this.props.onEnd;
    
    if(currentContent && 
        (currentContent.mediaType === Globals.YOUTUBE_VIDEO || 
         currentContent.mediaType === Globals.YOUTUBE_PLAYLIST)) {
      return (
        <div>
        
        <div className="row"><div className="col-xs-12">
        <YoutubePlayerAdapter currentContent={currentContent} onEnd={onEnd} /><hr/></div></div>
        
        {/*
           * Bug, TODO:
           * http://stackoverflow.com/questions/9947860/facebook-comments-disappear-when-re-visiting-dynamic-page
           * <div className="row"><div className="col-xs12"> <h3>Comments on
           * Facebook</h3> <div className="fb-comments"
           * data-href="https://www.facebook.com/Test-Parikshit-293482501003784/?fref=nf"
           * data-numposts="5"></div> </div></div>
           */}
        
        </div>
      );
    } else {
      return (<div>Cannot play content</div>);
    }
  }
  
});

