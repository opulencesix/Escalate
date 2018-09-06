/*
 * Top level layout of the application. Not to change much.
 */
var React = require('react');
var HeaderSection = require('./SectionHeader.react');
var MainSection = require('./SectionMain.react');
var FooterSection = require('./SectionFooter.react');

module.exports = React.createClass({
  render: function() {
    return (
      <div className='o6app'>
        <div><HeaderSection /></div>
        <div className="container"><MainSection /></div>
        <FooterSection />
      </div>
    );
  }
});
