/*
 * List of content modules. 
 * TODO currently duplicated from ContentList, later, refactor
 */
var React = require('react');
var ContentViewActionCreators = require('../actions/ContentViewActionCreators');
var PremiumContentModuleStore = require('../stores/PremiumContentModuleStore');
var UserStore = require('../stores/UserStore');
var Globals = require('../common/AppCommons');
var SearchBar = require('./common/ListFilterBar.react');
var FilteredContentList = require('./common/FilteredList.react');

var assign = require('object-assign');
var Router = require('../common/Router');


module.exports = React.createClass({
  getInitialState: function() {
    return {
      filterText: '',
      lockedContentModules: PremiumContentModuleStore.getLockedContentModules(),
      unlockedContentModules: PremiumContentModuleStore.getUnlockedContentModules()
    };
  },
  
  handleUserInput: function(filterText) {
    this.setState(assign(this.state,
            {filterText: filterText}));
  },
  
  componentDidMount: function() {
    PremiumContentModuleStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    PremiumContentModuleStore.removeChangeListener(this._onChange);
  },
  
  render: function() {
    return (
    <div>
      <h2>Premium Content Modules</h2>
      <SearchBar filterText={this.state.filterText} onUserInput={this.handleUserInput}/>
      <hr/>
      
      <h3> Locked Modules </h3>
      <FilteredContentList displayList={this.state.lockedContentModules} 
      filterText={this.state.filterText} _shouldBeFilteredFunc={this._shouldBeFilteredFunc}
      displaySectionGenFunc={this._displaySectionGenFuncLocked}
      />

      <h3> Unlocked Modules </h3>
      <FilteredContentList displayList={this.state.unlockedContentModules} 
      filterText={this.state.filterText} _shouldBeFilteredFunc={this._shouldBeFilteredFunc}
      displaySectionGenFunc={this._displaySectionGenFuncUnlocked}
      />

    </div>);
  },
  
  _onChange: function() {
    this.setState(this.getInitialState());
  },
  
  _shouldBeFilteredFunc: function(contentModule) {
    var filterTextLC = this.state.filterText.toLowerCase();
    if (contentModule.title.toLowerCase().indexOf(filterTextLC) === -1 && 
      (contentModule.description.toLowerCase().indexOf(filterTextLC) === -1)) {
      return true;
    }
    return false;
  },
  
  _displaySectionGenFuncLocked: function(contentModule) {
    return this._displaySectionCommon(contentModule, true);
  },
  
  _displaySectionGenFuncUnlocked: function(contentModule) {
    return this._displaySectionCommon(contentModule, false);
  },
  
  _displaySectionCommon: function(contentModule, canPurchaseFlag) {
    var onPurchaseClicked = this._purchaseClicked;
    
    return (
      <div key={contentModule.id}>
        <p><strong>{contentModule.title}</strong>, for <em>INR {contentModule.inrPriceRupees}</em></p>
        <p className="text-muted" dangerouslySetInnerHTML={{__html: contentModule.description}}></p>
        
        {canPurchaseFlag ? <button className="btn btn-default form-control-margin-right" 
          onClick={function(evt) {return onPurchaseClicked(contentModule.id, evt);}}> 
          Pay and Unlock </button> : "" }
        
        <hr/>
      </div>);

  },

  // Razorpay gateway functionality.
  _purchaseClicked: function(moduleId, evt) {
    var moduleInfo = PremiumContentModuleStore.getLockedModuleInfo(moduleId);
    var options = {
      "key": Globals.AppConfig.PM_GW_KEY,
      "amount": '' + Math.round(moduleInfo.inrPriceRupees * 100),
      "name": "OpulenceSix Digital",
      "description": "Unlock: " + moduleInfo.title,
      "image": "/img/o6Logo.png",
      "handler": function (response){
        ContentViewActionCreators.contentModulePurchased(moduleId, response.razorpay_payment_id);
        alert("Payment Successful!");
        Router.load("/#autoplay")
      },
      
      "prefill": {
        "email": UserStore.getUser().email,
      },
      "notes": {
        "txnDescription": UserStore.getUser().email + " purchasing module " + moduleId + ": " + moduleInfo.title
      },
      "theme": {
        "color": "#033f72"
      }
    };
    var rzp1 = new Razorpay(options);
    rzp1.open();
    evt.preventDefault();
  }

});
