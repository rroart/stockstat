import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import MiscBar from './MiscBar';
const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
];

const options2 = [
  { label: 'chocolate' },
  { label: 'strawberry' },
  { label: 'vanilla' }
];

class Misc extends PureComponent {
  state = {
    creators: [],
    years: []
  }
  bardvd : MiscBar;
  constructor() {
    super();
    Client.search("/misc/cd/creator", (creators) => {
      this.setState({
        creators: creators
      });
    });
    Client.search("/misc/cd/year", (years) => {
      this.setState({
        years: years
      });
    });
    console.log("creators" + this.state.creators);
}

  render() {
    const items = [
    'Google',
    'TED',
    'GitHub',
    'Big Think',
    'Microsoft',
  ]
  var creators2 = ConvertToSelect.convert(this.state.creators);
  var years2 = ConvertToSelect.convert(this.state.years);
  //this.bardvd = new MiscBar('dvd');
    return (
      <div>
    <MiscBar type='cd'/>
    <MiscBar type='dvd'/>
      </div>
    );
  }
/*
function search2(query, cb) {
return fetch(`http://localhost:8080/misc/cd/creator`, {
accept: 'application/json',
}).then(checkStatus)
.then(parseJSON)
.then(cb);
}
*/
}

export default Misc;
