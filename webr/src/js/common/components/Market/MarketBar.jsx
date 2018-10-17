import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'

class MarketBar extends PureComponent {
  type : string;
  state = {
    markets: ServiceResult,
  }
  constructor() {
    super();
    var serviceparam = new ServiceParam();
    serviceparam.market = '0';
    Client.search("/getmarkets", serviceparam, (markets) => {
      this.setState({
        markets: markets
      });
    });
    console.log(this.state.markets);
}

handleYearChange = (e) => {
  console.log(e);
  const value = e.value;
  var result;
  var serviceparam = new ServiceParam();
    Client.search("/getmarkets", serviceparam, (markets) => {
    this.setState({
      markets: markets
    });
  });
}

    handleChange(event) {
    console.log(event);
    console.log(event.value);
  }
  
  render() {
  //console.log(this.state.markets);
  var markets2 = ConvertToSelect.convert(this.state.markets.markets);
  console.log(markets2);
    return (
      <div>
      <Navbar>
        <Navbar.Header>
          <Navbar.Brand>
            <a href="#home">{this.type}</a>
          </Navbar.Brand>
        </Navbar.Header>
        <Nav>
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={this.handleChange}
        options={markets2}
      />
    </NavItem>
            </Nav>
          </Navbar>
      </div>
    );
  }
}

export default MarketBar;
