/*
 * The help slide show on the login and registration screen.
 */
var React = require('react');


module.exports = React.createClass({

  render: function() {
    return (
    <div id="myCarousel" className="carousel slide" data-ride="carousel">

      <ol className="carousel-indicators">
        <li data-target="#myCarousel" data-slide-to="0" className="active"></li>
        <li data-target="#myCarousel" data-slide-to="1"></li>
        <li data-target="#myCarousel" data-slide-to="2"></li>
      </ol>
      <div className="carousel-inner" role="listbox">
        <div className="item active">
          <img className="first-slide" src="img/escalatorLondon.jpg" alt="First slide" />
          <div className="container">
            <div className="carousel-caption">
              <h1>What is O6Escalate</h1>
              <p>OpulenceSix (or O6) Escalate is a personalized online digital media channel. Collates delightful, precious content from the world 
                for acclerated all-round betterment. Covering topics that matter today, relevant to everyone. 
                In a way never attempted before, through enriching entertainment.</p>
                <p><a className="btn btn-lg btn-primary" href="http://www.opulencesix.com/escalate" target="_blank" role="button">Learn more</a></p>
  
            </div>
          </div>
        </div>
        <div className="item">
          <img className="second-slide" src="img/topicAuthorWordCloud.jpg" alt="Second slide" />
          <div className="container">
            <div className="carousel-caption">
              <h1>What Content Exactly?</h1>
              <p>Ancient but has stood the test of time, or present-day. Universal topics like 
              Commonsense and prudence, business and economics, science and technology,
              arts and personal relationships, religion and spirituality, sociology and politics.</p>        
              <p><a className="btn btn-lg btn-primary" href="http://www.opulencesix.com/escalate" target="_blank" role="button">Learn more</a></p>
            </div>
          </div>
        </div>
        <div className="item">
          <img className="third-slide" src="img/diamonds.jpg" alt="Third slide" />
          <div className="container">
            <div className="carousel-caption">
              <h1>The Big Deal?</h1>
              <p> 
              Precisely targeted to user characteristics. Kids or adults, oriental or western. 
              From the masters, as it is, 
              rendered with polish for seamless, enjoyable comprehension. Your friend for life.</p>
              <p><a className="btn btn-lg btn-primary" href="http://www.opulencesix.com/escalate" target="_blank" role="button">Learn more</a></p>
            </div>
          </div>
        </div>
      </div>
      <a className="left carousel-control" href="#myCarousel" role="button" data-slide="prev">
        <span className="glyphicon glyphicon-chevron-left" aria-hidden="true"></span>
        <span className="sr-only">Previous</span>
      </a>
      <a className="right carousel-control" href="#myCarousel" role="button" data-slide="next">
        <span className="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
        <span className="sr-only">Next</span>
      </a>
    
    </div>
  );
  },
  
    
});
