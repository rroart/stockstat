import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-16-bootstrap-date-picker';

class MarketBar extends PureComponent {
  type : string;
  constructor(props) {
    super(props);
    console.log("here");
    console.log(props);
    console.log(this.props);
    var serviceparam = new ServiceParam();
    serviceparam.market = '0';
    Client.search("/getmarkets", serviceparam, (markets) => {
    console.log("here");
      console.log(markets);
      console.log(this.props);
      this.props.setmarkets(markets.markets);
    });
      var value = new Date().toISOString();
      console.log(this.props);
      this.props.setenddate(value);
    console.log("here");
    //console.log(this.state.markets);
}

handleYearChange = (e) => {
  console.log(e);
  console.log(this);
  const value = e.value;
  var result;
}

    handleChange(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //this.props.setmarket({market: event.value});
    props.setmarket(event.value);
  }
  
  render() {
    const { main } = this.props;
    console.log(main);
    const markets = main && main.markets ? main.markets : null;
    const startdate = main && main.startdate ? main.startdate : null;
    const enddate = main && main.enddate ? main.enddate : null;
  console.log(markets);
  var markets2 = ConvertToSelect.convert2(markets);
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
        onChange={e => this.handleChange(e, this.props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={2} href="#">
            Start date
	    <DatePicker id="startdatepicker" value={startdate} onChange={this.handleStartDateChange}/>
	    </NavItem>
          <NavItem eventKey={3} href="#">
            End date
	    <DatePicker id="enddatepicker" value={enddate} onChange={this.handleEndDateChange}/>
	    </NavItem>
            </Nav>
          </Navbar>
      </div>
    );
  }
}

export default MarketBar;
