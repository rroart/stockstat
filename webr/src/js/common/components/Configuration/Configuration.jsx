import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import ConfigTree from './ConfigTree';

function Configuration( { props, config, configname } ) {
  if (config === undefined ||config == null || config == "") {
    return(
      <div>
        <p>Empty</p>
      </div>);
  }
  return(
    <div>
      <p>Not empty</p>
      <ConfigTree props = { props } config =  { config } configname = { configname } />
    </div>);
}

export default Configuration;
