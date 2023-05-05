import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import IclijMarketBar from './IclijMarketBar';
import IclijEvolveBar from './IclijEvolveBar';

class IclijMarket extends PureComponent {
  constructor() {
    super();
  }

  render() {
    return (
      <div>
    <IclijMarketBar {...this.props}/>
      </div>
    );
  }
}

export default IclijMarket;
