import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-datepicker';

class IclijMarketBar extends PureComponent {
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
	props.setimarket(event.value);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'market', event.value ]);
    }
    
    handleChangeML(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//this.props.setmarket({market: event.value});
	props.setimarket(event.value);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'mlmarket', event.value ]);
    }
    
    resetMarket(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//this.props.setmarket({market: event.value});
	props.setimarket(null);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'market', null ]);
    }
    
    resetML(event, props) {
	console.log(event);
	console.log(event.value);
	console.log(props);
	//this.props.setmarket({market: event.value});
	props.setmlmarket(null);
	//props.setconfigvalue([ 'market', event.value ]);
	props.seticonfigvalue([ 'mlmarket', null ]);
    }
    
    handleStartDateChange(event, props) {
	console.log(event);
	console.log(props);
	props.setstartdate(event);
        props.seticonfigvalue([ 'startdate', event ]);
    }
    
    handleEndDateChange(event, props) {
	console.log(event);
	console.log(props);
	props.setenddate(event);
        props.seticonfigvalue([ 'enddate', event ]);
    }
    
    resetStartDate(event, props) {
	console.log(event);
	console.log(props);
	props.setstartdate(null);
        props.seticonfigvalue([ 'startdate', null ]);
    }
    
    resetEndDate(event, props) {
	console.log(event);
	console.log(props);
	props.setenddate(null);
        props.seticonfigvalue([ 'enddate', null ]);
    }
    
    getContent(event, props) {
	console.log(event);
	console.log(props);
	console.log(props.main.imarket);
	props.getcontent2(props.main.iconfig, props.main.imarket, props);
    }
    
    getContent(event, props) {
	props.getcontent(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentEvolve(event, props) {
	props.getcontentevolve(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentDataset(event, props) {
	props.getcontentdataset(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentCrosstest(event, props) {
	props.getcontentcrosstest(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentImprove(event, props) {
	props.getcontentimprove(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentMachineLearning(event, props) {
	props.getcontentmachinelearning(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentFilter(event, props) {
	props.getcontentfilter(props.main.iconfig, props.main.imarket, props);
    }
    
    getContentAboveBelow(event, props) {
	props.getcontentabovebelow(props.main.iconfig, props.main.imarket, props);
    }
    
    getSingleMarket(event, props) {
	props.getsinglemarket(props.main.iconfig, props.main.imarket, props, false);
    }
    
    getSingleMarketLoop(event, props) {
	props.getsinglemarket(props.main.iconfig, props.main.imarket, props, true);
    }
    
    getImproveProfit(event, props) {
	props.getimproveprofit(props.main.iconfig, props.main.imarket, props);
    }
    
    getImproveAboveBelow(event, props) {
	props.getimproveabovebelow(props.main.iconfig, props.main.imarket, props);
    }
    
    getVerify(event, props) {
	props.getverify(props.main.iconfig, props.main.imarket, props, false);
    }
    
    getVerifyLoop(event, props) {
	props.getverify(props.main.iconfig, props.main.imarket, props, true);
    }
    
    render() {
	const { main } = this.props;
	console.log(main);
	const markets = main && main.markets ? main.markets : null;
	const startdate = main && main.startdate ? main.startdate : null;
	const enddate = main && main.enddate ? main.enddate : null;
	console.log(process.env);
	console.log(process.porti);
	console.log(main.imarket);
	console.log(main.iconfig);
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
			    onChange={e => this.resetMarket(e, this.props)}
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
		</Nav>
	      </Navbar>
	      <Navbar>
		<Navbar.Header>
		  <Navbar.Brand>
		    <a href="#home">{this.type}</a>
		  </Navbar.Brand>
		</Navbar.Header>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContent(e, this.props)
		      }
		      >
		      Get find profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentMachineLearning(e, this.props)
		      }
		      >
		      Get machine learning data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentEvolve(e, this.props)
		      }
		      >
		      Get evolve data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentImprove(e, this.props)
		      }
		      >
		      Get improve profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentDataset(e, this.props)
		      }
		      >
		      Get dataset data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentCrosstest(e, this.props)
		      }
		      >
		      Get crosstest data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentFilter(e, this.props)
		      }
		      >
		      Get filter data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={7} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getContentAboveBelow(e, this.props)
		      }
		      >
		      Get above below data
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Navbar.Header>
		  <Navbar.Brand>
		    <a href="#home">{this.type}</a>
		  </Navbar.Brand>
		</Navbar.Header>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getVerify(e, this.props)
		      }
		      >
		      Run and get find profit verification data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getVerifyLoop(e, this.props)
		      }
		      >
		      Run and get find profit verification data loop
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Navbar.Header>
		  <Navbar.Brand>
		    <a href="#home">{this.type}</a>
		  </Navbar.Brand>
		</Navbar.Header>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getSingleMarket(e, this.props)
		      }
		      >
		      Run and get find profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getSingleMarketLoop(e, this.props)
		      }
		      >
		      Run and get find profit data loop
		    </Button>
		  </NavItem>
		</Nav>
	      </Navbar>
	      <Navbar>
		<Navbar.Header>
		  <Navbar.Brand>
		    <a href="#home">{this.type}</a>
		  </Navbar.Brand>
		</Navbar.Header>
		<Nav>
		  <NavItem eventKey={6} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getImprove(e, this.props)
		      }
		      >
		      Run and get single improve profit data
		    </Button>
		  </NavItem>
		  <NavItem eventKey={7} href="#">
		    <Button
		      bsStyle="primary"
		      onClick={
			  (e) => this.getImproveAboveBelow(e, this.props)
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
}

export default IclijMarketBar;
