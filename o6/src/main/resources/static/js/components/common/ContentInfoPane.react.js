/*
 * Used to play single standalone content only, not upcoming content
 */
var React = require('react');
var ContentStore = require('../../stores/ContentStore');
var ContentViewActionCreators = require('../../actions/ContentViewActionCreators');
var MoreLessText = require('./MoreLessText.react');

module.exports = React.createClass({
  
  getInitialState: function() {
    return {
      fullDescriptionFlag : false
    };
  },
  
  componentDidMount: function() {
    ContentStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContentStore.removeChangeListener(this._onChange);
  },
  
  render: function() {
    var content = this.props.content;
    if(!content) {
      return (<div> No information </div>);
    }
    
    return (
      <div className="panel panel-primary">

        <div className="panel-heading panel-title">
          {this.props.content.title}
        </div>
        
        <div className="panel-body">
          <p><strong>By:</strong> {this.props.content.authors}</p>
          <p className="text-muted"><MoreLessText text={this.props.content.description} /></p>
          
          <ul className="list-inline">
          {this.props.onSkipped ? (<li><button className="btn btn-default btn-small" onClick={this.props.onSkipped}> Skip <i className="fa fa-angle-right fa-fw" aria-hidden="true"></i></button></li>) : (<li/>) }          
          <li><button className="btn btn-default btn-small" onClick={this._toggleFavorite}><i className={this._getFavoriteIcon()} aria-hidden="true" title="Toggle favorite"></i></button></li>
          <li><button className="btn btn-default btn-small" onClick={this._onOpenContentText}><i className="fa fa-book fa-fw" aria-hidden="true" title="Read"></i></button></li>
          </ul>

        </div>
    
      </div>
    );
  },
  
  _onChange: function() {
    this.forceUpdate();
  },
  
  _getFavoriteIcon: function() {
    return "fa fa-fw " + (this.props.content.favorite ? "fa-star" : "fa-star-o " );
  },
  
  _toggleFavorite: function() {
    ContentViewActionCreators.conveyContentFavoriteToggled(this.props.content.contentId);
  },
  
  _buildDescription: function() {
    if(this.state.fullDescriptionFlag) {
      return this.props.content.description;
    } else {
      return this.props.content.description.substring(0, 100) + '...';
    }
  },
  
  _getReadMoreLessLinkText: function() {
    return this.state.fullDescriptionFlag ? " Show less " : " Show more ";
  },
  
  _toggleExpanded: function() {
    this.setState({fullDescriptionFlag: !this.state.fullDescriptionFlag});
  },
  
  _onOpenContentText: function() {
    window.open(this.props.content.textUrl, '_blank');
  }

  

});

