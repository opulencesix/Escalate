/*
 * Generic form input component that allows textboxes, checkboxes and selects,
 * and returns the current form value as an object.
 * 
 * TODO: After some time, this has actually morphed to a Profile Update form,
 * might have lost its generality. Need to change the naming accordingly.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var Utils = require('../../common/CommonUtils');

module.exports = React.createClass({
  
  render: function() {
    var fields = [];
    var schema = this.props.schema;
    var currentValue = this.props.currentValue;
    for(var prop in schema) {
      fields.push(<div key={prop} className="form-group"><label htmlFor={prop} className="col-sm-offset-1 col-sm-1 text-right control-label">{schema[prop].label}</label><div className="col-sm-4 form-control-margin-bottom">
        {this._createField(prop, schema[prop], currentValue[prop])}</div></div>);
    }
    return(
      <form >
        {fields}
        
        <div className="row">
        <div className="col-sm-offset-2 col-sm-5">
        <div className="btn-group">
        <button className="btn btn-default" type="button" onClick={this.props.onCancelClick} > Cancel </button>
        <button className="btn btn-danger" type="button" onClick={this.props.onDeleteClick} > <i className="fa fa-trash" /> Delete </button>
        <button className="btn btn-primary" type="button" onClick={this.props.onClick} > {this.props.buttonName} </button>
        </div>
        </div>
        </div>
        
      </form>
    );
  },

  _createField: function(fieldName, schema, currentValue) {
    var initialVal = currentValue ? currentValue : schema.defaultVal;
    switch(this._getFormFieldType(schema.type, schema.modifiers)) {
    case "selectBox":
      return this._createSelectBox(fieldName, schema.modifiers, initialVal);
    case "multiSelectBox": 
      return this._createMultiSelectBox(fieldName, schema.modifiers, initialVal);
    case "textInput": 
      return this._createTextInput(fieldName, initialVal);
    case "textArea": 
      return this._createTextArea(fieldName, initialVal);
    case "checkbox": 
      return this._createCheckbox(fieldName, initialVal);
      default:
    }
  },
  
  // Get the UI field type based on datatype passed
  _getFormFieldType: function(dataType, modifier) {
    if(modifier) {
      if(modifier.oneOf) {
        return "selectBox";
      } else if(modifier.oneOrMore) {
       return "multiSelectBox";
      } 
    } else {
      if(dataType === 'string') {
        return "textInput";
      } else if(dataType === 'text') {
        return "textArea";
      } else if(dataType === 'boolean') {
        return "checkbox";
      } 
    }
  },
  
  /*
   * Internal create UI element functions
   */
  _createSelectBox: function(fieldName, schema, initialVal) {
    var options = schema.oneOf.map(function(elem) {
      return (<option key={elem} value={elem}> {Utils.toTitleCase(elem)} </option>);
    });
    return (
      <select key={fieldName} ref={fieldName} defaultValue={initialVal} className="form-control">
        {options}
      </select>);

  },
  
  _createMultiSelectBox: function(fieldName, schema, initialVal) {
    var options = schema.oneOrMore.map(function(elem) {
      return (<option key={elem} value={elem} > {Utils.toTitleCase(elem)} </option>);
    });
    return (
      <select key={fieldName} ref={fieldName} defaultValue={initialVal} multiple={true} className="form-control">
        {options}
      </select>);
    
  },
  
  _createTextInput: function(fieldName, initialVal) {
    return (<input ref={fieldName} key={fieldName} type="text" name={fieldName} defaultValue={initialVal} className="form-control" />);
  },
  
  _createTextArea: function(fieldName, initialVal) {
    return (<textarea rows="3" ref={fieldName} key={fieldName} name={fieldName} defaultValue={initialVal} className="form-control" />);
  },
  
  _createCheckbox: function(fieldName, initialVal) {
    return (
      <div key={fieldName} >
          <input type="checkbox" ref={fieldName}  defaultChecked={initialVal ? true : false} />
      </div>
      );
  },
  
  /*
   * Iterate over all the form elements collect their current values and return
   */
  getFieldData: function() {
    var schema = this.props.schema;
    var fieldData = Object.create(null);

    // Access the DOM node from the reference name of the object, equal to the
    // element name.
    for(var prop in schema) {
      switch(this._getFormFieldType(schema[prop].type, schema[prop].modifiers)) {
      case "selectBox":
      case "textInput": 
        fieldData[prop] = ReactDOM.findDOMNode(this.refs[prop]).value.trim();
        break;
      case "textArea": 
        fieldData[prop] = ReactDOM.findDOMNode(this.refs[prop]).value.trim();
        break;
      case "multiSelectBox": 
        fieldData[prop] = Utils.filterObjectVals(ReactDOM.findDOMNode(this.refs[prop]).options, 
                function(elem) {return elem.selected ? true :false;}).map(function(elem) {return elem.value});
        if(fieldData[prop].length===0) {
          fieldData[prop] = schema[prop].defaultVal;
        }
        break;
      case "checkbox": 
        fieldData[prop] = ReactDOM.findDOMNode(this.refs[prop]).checked;
        default:
      }
    }

    return fieldData;    
  }
  
});
