/*
 * Youtube react component originally taken from:
 * https://github.com/mdebbar/YoutubePlayer.react
 */
var React   = require('react');
var Globals = require('../../common/AppCommons')

module.exports = React.createClass({
  propTypes: {
    autoplay: React.PropTypes.bool,
    theme: React.PropTypes.oneOf(['dark', 'light']),
    width: React.PropTypes.number,
    height: React.PropTypes.number,
    youtubeId: React.PropTypes.string.isRequired,
    mediaType: React.PropTypes.string.isRequired,
    onSwitchVideo: React.PropTypes.func,

    // Player events:
    onPlay: React.PropTypes.func,
    onPause: React.PropTypes.func,
    onBuffering: React.PropTypes.func,
    onEnd: React.PropTypes.func
  },

  getDefaultProps: function() {
    return {
      autoplay: false,
      theme: 'dark',
      width: 640,
      height: 390,
      onSwitchVideo: function () {}
    };
  },
  
  /* Public API for controlling the player */

  play: function() {
    this.player && this.player.playVideo();
  },

  pause: function() {
    this.player && this.player.pauseVideo();
  },

  mute: function() {
    this.player && this.player.mute();
  },

  unMute: function() {
    this.player && this.player.unMute();
  },

  /* Life cycle methods */

  componentWillMount: function() {
    this.playerID = Globals.PLAYER_ID_PREFIX + String(Globals.playerSeqID++);
    Globals.ytLoadAPI(this._onAPIReady);
  },

  componentWillUnmount: function() {
    this.player && this.player.destroy();
  },

  componentWillReceiveProps: function(props) {
    this.player && this._maybeUpdatePlayer(props.youtubeId, props.mediaType);
  },

  _maybeUpdatePlayer: function(youtubeId, mediaType) {
    if (this.props.youtubeId !== youtubeId) {
      // If it's a different video/playlist => stop current video + load the new one!
      // (loading will automatically play the video)
      this.player.stopVideo();
      if(mediaType == Globals.YOUTUBE_VIDEO) {
        this.player.loadVideoById(youtubeId);        
      } else if(mediaType == Globals.YOUTUBE_PLAYLIST) {
        this.player.loadPlaylist({list: youtubeId});                
      }
    }
  },

  /*
   * Avoid component update after first load, since all interactions are via the
   * youtube player object, iframe is only for size manipulation. Reloading
   * actually causes issues in the player event handling.
   */
  shouldComponentUpdate: function(nextProps, nextState) {
    return false;
  },
  
  render: function() {
    // var {autoplay, videoID, width, height, theme, ...other} = this.props;

    if(this.props.mediaType === Globals.YOUTUBE_VIDEO) {
      return (
              <div className="embed-responsive embed-responsive-16by9">
              <iframe id={this.playerID} className="embed-responsive-item videoIframe"
                src={'https://www.youtube.com/embed/'+this.props.youtubeId+'?theme='+this.props.theme+'&autoplay=1&modestbranding=1&enablejsapi=1&origin=' + window.location.origin} allowFullScreen >
              </iframe>
              </div> );      
    } else if (this.props.mediaType === Globals.YOUTUBE_PLAYLIST) {
      return (
              <div className="embed-responsive embed-responsive-16by9">
              <iframe id={this.playerID} className="embed-responsive-item videoIframe"
                src={'https://www.youtube.com/embed?listType=playlist&list='+this.props.youtubeId+'&theme='+this.props.theme+'&autoplay=1&modestbranding=1&enablejsapi=1&origin=' + window.location.origin} allowFullScreen >
              </iframe>
              </div> );      
    }
  },

  /* Handling the API and the player */

  _onAPIReady: function() {
    if (!this.isMounted()) {
      return;
    }

    new YT.Player(this.playerID, {
      events: {
        onReady: this._onPlayerReady,
        onStateChange: this._onPlayerStateChange
      }
    });
  },

  _onPlayerReady: function(event) {
    if (!this.isMounted()) {
      return;
    }
    this.player = event.target;
    this._maybeUpdatePlayer(this.props.youtubeId, this.props.mediaType);
  },

  _onPlayerStateChange: function(event) {
    // Respond to player events
    // Possible states:
    // {UNSTARTED: -1, ENDED: 0, PLAYING: 1, PAUSED: 2, BUFFERING: 3, CUED: 5}
    switch (event.data) {
      case YT.PlayerState.UNSTARTED:
        var videoID = event.target.getVideoData().video_id;
        if (videoID !== this.props.youtubeId) {
          this.props.onSwitchVideo(videoID);
        }                    
        break;
      case YT.PlayerState.PLAYING:
        this.props.onPlay && this.props.onPlay();
        break;
      case YT.PlayerState.PAUSED:
        this.props.onPause && this.props.onPause();
        break;
      case YT.PlayerState.ENDED:
        if(this.props.mediaType === Globals.YOUTUBE_VIDEO) {
          this.props.onEnd && this.props.onEnd();          
        } else if(this.props.mediaType === Globals.YOUTUBE_PLAYLIST) {
          var onEndFunc = this.props.onEnd;
          var tempPlayer = this.player;
          setTimeout(function(){
            // If new video is not yet loaded, after sleep, then it is last
            // video of playlist.
            if(tempPlayer.getPlayerState() == YT.PlayerState.ENDED){
              onEndFunc && onEndFunc();
            }
          } , 2000);
        }
        break;
      case YT.PlayerState.BUFFERING:
        this.props.onBuffering && this.props.onBuffering();
        break;
    }
  }
});

