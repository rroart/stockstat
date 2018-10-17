import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import MarketBar from './MarketBar';

class Market extends PureComponent {
  state = {
    markets: ServiceResult,
  }
  constructor() {
    super();
  }

  render() {
    return (
      <div>
    <MarketBar {...this.props}/>
      </div>
    );
  }
}

export default Market;
