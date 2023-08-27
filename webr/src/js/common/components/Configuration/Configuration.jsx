import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import ConfigTree from './ConfigTree';

class Configuration extends PureComponent {
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
  <ConfigTree {...this.props}/>
  </div>);
  }
}

export default Configuration;
