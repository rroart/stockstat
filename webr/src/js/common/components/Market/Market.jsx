import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import MarketBar from './MarketBar';
import EvolveBar from './EvolveBar';

class Market extends PureComponent {
  constructor() {
    super();
  }

  render() {
    return (
      <div>
    <MarketBar {...this.props}/>
    <EvolveBar {...this.props}/>
      </div>
    );
  }
}

export default Market;
