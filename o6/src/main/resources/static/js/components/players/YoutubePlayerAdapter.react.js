/*
 * Embed the open source youtubeplayer component.
 */
var React = require('react');
var YoutubePlayer = require('./YoutubePlayer.react');

module.exports = React.createClass({
  componentWillMount: function() {
    if(this.props.onEnd) {
      this._onEnd = this.props.onEnd;
    }
  },
  
  render: function() {
    return (
      <YoutubePlayer
        ref="player"
        // This is the ID of the video:
        youtubeId={this.props.currentContent.externalId} // XxVg_s8xAms"
        mediaType={this.props.currentContent.mediaType} // youtubevideo, youtubeplaylist
        width={640}
        height={390}
        // Called when the user selects a video from the recommendations at the
        // end
        onSwitchVideo={this._onSwitch}
        // Called when the video starts playing
        onPlay={this._onPlay}
        // Called when the video is paused
        onPause={this._onPause}
        // Called when the end of the video is reached
        onEnd={this._onEnd}
      />
    );
  },

  _onSwitch: function(videoID) {
    console.log('The user switched to the video:', videoID);
  },

  _onPlay: function() {
    console.log('The video is now playing');
  },

  _onPause: function() {
    console.log('The video is now paused');
  },

  _onEnd: function() {
    console.log('The video end is reached');
    // Let's do something fun :)
    // When the end is reached, let's play the video again, muted!
    // this.refs.player.play();
    // this.refs.player.mute();
  }
});

