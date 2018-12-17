import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';

const options = [
  { value: 'chocolate', label: 'Chocolate' },
  { value: 'strawberry', label: 'Strawberry' },
  { value: 'vanilla', label: 'Vanilla' }
];

const options2 = [
  { label: 'chocolate' },
  { label: 'strawberry' },
  { label: 'vanilla' }
];

class MiscBar extends PureComponent {
  type : string;
  state = {
    creators: [],
    years: []
  }
  constructor(props) {
    super(props);
    this.type = props.type;
    Client.search("/misc/" + this.type + "/creator", (creators) => {
      this.setState({
        creators: creators
      });
    });
    Client.search("/misc/" + this.type + "/year", (years) => {
      this.setState({
        years: years
      });
    });
    //console.log("creators" + this.state.creators);
}

handleYearChange = (e) => {
  console.log(e);
  const value = e.value;
  var result;
  Client.search("/misc/" + this.type + "/year/" + value, (result) => {
    this.setState({
      years: years
    });
  });
}

    handleChange(event) {
    console.log(event);
    console.log(event.value);
  }
  
  render() {
  var creators2 = ConvertToSelect.convert(this.state.creators);
  var years2 = ConvertToSelect.convert(this.state.years);
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
        options={creators2}
      />
    </NavItem>
    <NavItem eventKey={2} href="#">
      Year
            <Select options="[{size:'5'}]"
              onChange={this.handleYearChange}
              options={years2}
            />
        </NavItem>
        <NavItem eventKey={3} href="#">
          Search
          <FormControl
            type="text"
            value={this.state.value}
            placeholder="Enter text"
            onChange={this.handleChange}
          />
        </NavItem>
            </Nav>
          </Navbar>
      </div>
    );
  }
}

export default MiscBar;
