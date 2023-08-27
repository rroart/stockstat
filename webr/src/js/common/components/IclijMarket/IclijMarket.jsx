import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import IclijMarketBar from './IclijMarketBar';
import IclijEvolveBar from './IclijEvolveBar';

function IclijMarket( { props } ) {
    return (
      <div>
    <IclijMarketBar props = { props }/>
      </div>
    );
  }


export default IclijMarket;
