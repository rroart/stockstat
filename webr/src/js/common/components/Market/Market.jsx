import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import MarketBar from './MarketBar';
import EvolveBar from './EvolveBar';

function Market( { props } ) {
   return (
      <div>
    <MarketBar props = { props }/>
    <EvolveBar props = { props }/>
      </div>
    );
  }

export default Market;
