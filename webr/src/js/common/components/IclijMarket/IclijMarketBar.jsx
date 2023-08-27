import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-datepicker';

function IclijMarketBar( { props } ) {
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
	props.setimarket(event.value);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'market', event.value ]);
    }

   function  handleChangeML(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//props.setmarket({market: event.value});
	props.setimarket(event.value);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'mlmarket', event.value ]);
    }

  function   resetMarket(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//props.setmarket({market: event.value});
	props.setimarket(null);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'market', null ]);
    }

    function resetML(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//props.setmarket({market: event.value});
	props.setmlmarket(null);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'mlmarket', null ]);
    }

    function handleStartDateChange(event, props) {
	console.log(event);
	console.log(props);
	props.setstartdate(event);
        props.seticonfigvalue([ 'startdate', event ]);
    }

   function  handleEndDateChange(event, props) {
	console.log(event);
	console.log(props);
	props.setenddate(event);
        props.seticonfigvalue([ 'enddate', event ]);
    }

   function  resetStartDate(event, props) {
	console.log(event);
	console.log(props);
	props.setstartdate(null);
        props.seticonfigvalue([ 'startdate', null ]);
    }

  function   resetEndDate(event, props) {
	console.log(event);
	console.log(props);
	props.setenddate(null);
        props.seticonfigvalue([ 'enddate', null ]);
    }

   function  getContent(event, props) {
	console.log(event);
	console.log(props);
	console.log(props.main.imarket);
	props.getcontent2(props.main.iconfig, props.main.imarket, props);
    }

  function   getContent(event, props) {
	props.getcontent(props.main.iconfig, props.main.imarket, props);
    }

   function  getContentEvolve(event, props) {
	props.getcontentevolve(props.main.iconfig, props.main.imarket, props);
    }

    function getContentDataset(event, props) {
	props.getcontentdataset(props.main.iconfig, props.main.imarket, props);
    }

   function  getContentCrosstest(event, props) {
	props.getcontentcrosstest(props.main.iconfig, props.main.imarket, props);
    }

   function  getContentImprove(event, props) {
	props.getcontentimprove(props.main.iconfig, props.main.imarket, props);
    }

   function  getContentMachineLearning(event, props) {
	props.getcontentmachinelearning(props.main.iconfig, props.main.imarket, props);
    }

  function   getContentFilter(event, props) {
	props.getcontentfilter(props.main.iconfig, props.main.imarket, props);
    }

   function  getContentAboveBelow(event, props) {
	props.getcontentabovebelow(props.main.iconfig, props.main.imarket, props);
    }

  function   getSingleMarket(event, props) {
	props.getsinglemarket(props.main.iconfig, props.main.imarket, props, false);
    }

  function   getSingleMarketLoop(event, props) {
	props.getsinglemarket(props.main.iconfig, props.main.imarket, props, true);
    }

  function   getImproveProfit(event, props) {
	props.getimproveprofit(props.main.iconfig, props.main.imarket, props);
    }

   function  getImproveAboveBelow(event, props) {
	props.getimproveabovebelow(props.main.iconfig, props.main.imarket, props);
    }

   function  getVerify(event, props) {
	props.getverify(props.main.iconfig, props.main.imarket, props, false);
    }

   function  getVerifyLoop(event, props) {
	props.getverify(props.main.iconfig, props.main.imarket, props, true);
    }

	const { main } = props;
	console.log(main);
	const markets = main && main.markets ? main.markets : null;
	const startdate = main && main.startdate ? main.startdate : null;
	const enddate = main && main.enddate ? main.enddate : null;
	//console.log(process.env);
	//console.log(process.porti);
	console.log(main.imarket);
	console.log(main.iconfig);
	//console.log(main.config.get('market'));
	console.log(markets);
	console.log(markets.length);
	console.log(typeof(markets));
	console.log(Object.keys(markets));
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
			    onChange={e => resetMarket(e, props)}
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
		</Nav>
	      </Navbar>
	      <Navbar>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContent(e, props)
		      }
		      >
		      Get find profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentMachineLearning(e, props)
		      }
		      >
		      Get machine learning data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentEvolve(e, props)
		      }
		      >
		      Get evolve data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentImprove(e, props)
		      }
		      >
		      Get improve profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentDataset(e, props)
		      }
		      >
		      Get dataset data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentCrosstest(e, props)
		      }
		      >
		      Get crosstest data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentFilter(e, props)
		      }
		      >
		      Get filter data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={7} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getContentAboveBelow(e, props)
		      }
		      >
		      Get above below data
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getVerify(e, props)
		      }
		      >
		      Run and get find profit verification data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getVerifyLoop(e, props)
		      }
		      >
		      Run and get find profit verification data loop
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getSingleMarket(e, props)
		      }
		      >
		      Run and get find profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getSingleMarketLoop(e, props)
		      }
		      >
		      Run and get find profit data loop
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getImprove(e, props)
		      }
		      >
		      Run and get single improve profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={7} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => getImproveAboveBelow(e, props)
		      }
		      >
		      Run and get single improve above below data
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	    </div>
	);
    }

export default IclijMarketBar;
