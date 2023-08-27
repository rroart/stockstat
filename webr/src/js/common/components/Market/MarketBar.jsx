import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-datepicker';

function MarketBar( { props }) {

function handleYearChange() {
  console.log(e);
  console.log(this);
  const value = e.value;
  var result;
}

    function handleChange(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //props.setmarket({market: event.value});
    props.setmarket(event.value);
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'market', event.value ]);
  }

    function handleChangeML(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //props.setmarket({market: event.value});
    props.setmarket(event.value);
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'mlmarket', event.value ]);
  }

    function resetML(event, props) {
    console.log(event);
    console.log(event.value);
    console.log(props);
    //props.setmarket({market: event.value});
    props.setmlmarket(null);
    //props.setconfigvalue([ 'market', event.value ]);
    props.setconfigvalue([ 'mlmarket', null ]);
  }

    function handleStartDateChange(event, props) {
    console.log(event);
    console.log(props);
    props.setstartdate(event);
  }

    function handleEndDateChange(event, props) {
    console.log(event);
    console.log(props);
    props.setenddate(event);
    props.setconfigvalue([ 'enddate', event ]);
  }

    function resetStartDate(event, props) {
    console.log(event);
    console.log(props);
    props.setstartdate(null);
  }

    function resetEndDate(event, props) {
    console.log(event);
    console.log(props);
    props.setenddate(null);
    props.setconfigvalue([ 'enddate', null ]);
  }

    function getMarketData(event, props) {
    console.log(event);
    console.log(props);
    console.log(props.main.market);
    props.getcontent(props.main.config, props.main.market, props);
  }

   const { main } = props;
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
  var markets2 = ConvertToSelect.convert3(markets);
  console.log(markets2);
    return (
      <div>
      <Navbar>
          <Nav>
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={e => handleChange(e, props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={e => handleChangeML(e, props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={1} href="#">
            Name
            <Select options="[{size:'5'}]"
        onChange={e => resetML(e, props)}
        options={markets2}
      />
    </NavItem>
          <NavItem eventKey={2} href="#">
            Start date
	    <DatePicker id="startdatepicker" value={startdate} onChange={e => handleStartDateChange(e, props)}/>
	    </NavItem>
          <NavItem eventKey={3} href="#">
            End date
	    <DatePicker id="enddatepicker" value={enddate} onChange={e => handleEndDateChange(e, props)}/>
	    </NavItem>
          <NavItem eventKey={4} href="#">
        <Button
       bsStyle="primary"
       onClick={ (e) => resetStartDate(e, props) }
     >
     Reset start date
     </Button>
     </NavItem>
          <NavItem eventKey={5} href="#">
        <Button
       bsStyle="primary"
       onClick={ (e) => resetEndDate(e, props) }
     >
     Reset end date
     </Button>
     </NavItem>
          <NavItem eventKey={6} href="#">
        <Button
       bsStyle="primary"
       onClick={
         (e) => getMarketData(e, props)
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

export default MarketBar;
