import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import IclijConfigTree from './IclijConfigTree';

class IclijConfiguration extends PureComponent {
  constructor(props) {
    super(props);
    console.log("here");
    console.log(props);
    console.log(this.props);
    console.log("here");
    //console.log(this.state.markets);
}

  render() {
    if (true) return;
  return(
  <div>
  <p>Empty</p>
  <IclijConfigTree {...this.props}/>
  </div>);
  }
}

export default IclijConfiguration;
