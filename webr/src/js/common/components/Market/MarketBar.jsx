import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-datepicker';

class MarketBar extends PureComponent {
  type;
  constructor(props) {
    super(props);
    console.log("here");
    console.log(props);
    console.log(this.props);
      var value = new Date().toISOString();
      console.log(this.props);
      this.props.setenddate(value);
    console.log("here");
    //console.log(this.state.markets);
}

/*
  componentDidMount() {
    this.props.getMarkets();
  }
  */

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
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'market', event.value ]);
  }
  
    handleChangeML(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //this.props.setmarket({market: event.value});
    props.setmarket(event.value);
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'mlmarket', event.value ]);
  }
  
    resetML(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //this.props.setmarket({market: event.value});
    props.setmlmarket(null);
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'mlmarket', null ]);
  }
  
    handleStartDateChange(event, props) {
    console.log(event);
    console.log(props);
    props.setstartdate(event);
  }
  
    handleEndDateChange(event, props) {
    console.log(event);
    console.log(props);
    props.setenddate(event);
    props.setconfigvalue([ 'enddate', event ]);
  }
  
    resetStartDate(event, props) {
    console.log(event);
    console.log(props);
    props.setstartdate(null);
  }
  
    resetEndDate(event, props) {
    console.log(event);
    console.log(props);
    props.setenddate(null);
    props.setconfigvalue([ 'enddate', null ]);
  }
  
    getMarketData(event, props) {
    console.log(event);
    console.log(props);
    console.log(props.main.market);
    props.getcontent(props.main.config, props.main.market, props);
  }
  
  render() {
    const { main } = this.props;
    console.log(main);
    const markets = main && main.markets ? main.markets : null;
    const startdate = main && main.startdate ? main.startdate : null;
    const enddate = main && main.enddate ? main.enddate : null;
    console.log(main.market);
    console.log(main.config);
    //console.log(main.config.get('market'));
  console.log(markets);
  console.log(startdate);
  console.log(enddate);
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
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={e => this.handleChangeML(e, this.props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={e => this.resetML(e, this.props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={2} href="#">
            Start date
	    <DatePicker id="startdatepicker" value={startdate} onChange={e => this.handleStartDateChange(e, this.props)}/>
	    </NavItem>
          <NavItem eventKey={3} href="#">
            End date
	    <DatePicker id="enddatepicker" value={enddate} onChange={e => this.handleEndDateChange(e, this.props)}/>
	    </NavItem>
          <NavItem eventKey={4} href="#">
        <Button
       bsStyle="primary"
       onClick={ (e) => this.resetStartDate(e, this.props) }
     >
     Reset start date
     </Button>
     </NavItem>
          <NavItem eventKey={5} href="#">
        <Button
       bsStyle="primary"
       onClick={ (e) => this.resetEndDate(e, this.props) }
     >
     Reset end date
     </Button>
     </NavItem>
          <NavItem eventKey={6} href="#">
        <Button
       bsStyle="primary"
       onClick={
         (e) => this.getMarketData(e, this.props)
       }
     >
     Get market data
     </Button>
     </NavItem>
     </Nav>
          </Navbar>
      </div>
    );
  }
}

export default MarketBar;
