import React, { PureComponent } from 'react';

import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-date-picker';

class IclijEvolveBar extends PureComponent {
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

  resetRecommender(event, props) {

  }

  resetMLMACD(event, props) {
    props.setconfigvaluemap([ 'aggregators.mlmacd.mlconfig', null ]);
  }

  resetMlindicator(event, props) {
    props.setconfigvaluemap([ 'aggregators.indicator.mlconfig', null ]);
  }

  resetPredictorLSTM(event, props) {
    props.setconfigvaluemap([ 'machinelearning.tensorflow.lstm.config', null ]);
  }

  evolveRecommender(event, props) {
    props.getevolve(['getevolverecommender', false, props.main.config, '']);
  }

  evolveMLMACD(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'mlmacd']);
  }

  evolveMlindicator(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'mlindicator']);
  }

  evolvePredictorLSTM(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'predictorlstm']);
  }

  evolveAndSetRecommender(event, props) {
    props.getevolve(['getevolverecommender', true, props.main.config, '']);
  }

  evolveAndSetMLMACD(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'mlmacd']);
  }

  evolveAndSetMlindicator(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'mlindicator']);
  }

  evolveAndSetPredictorLSTM(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'predictorlstm']);
  }

/*
  componentDidMount() {
    this.props.getMarkets();
  }
  */

  render() {
    const { main } = this.props;
    console.log(main);
    const markets = main && main.markets ? main.markets : null;
    const startdate = main && main.startdate ? main.startdate : null;
    const enddate = main && main.enddate ? main.enddate : null;
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
	    <Button bsStyle="primary" onClick={ (e) => this.resetRecommender(e, this.props) } >Reset recommender</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ e => this.evolveRecommender(e, this.props) } >Evolve recommender</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => this.evolveAndSetRecommender(e, this.props) } >Evolve recommender and set</Button>
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
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.resetMLMACD(e, this.props) } >Reset MLMACD</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.evolveMLMACD(e, this.props) } >Evolve MLMACD</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => this.evolveAndSetMLMACD(e, this.props) } >Evolve MLMACD and set</Button>
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
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.resetMlindicator(e, this.props) } >Reset mlindicator</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.evolveMlindicator(e, this.props) } >Evolve mlindicator</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => this.evolveAndSetMlindicator(e, this.props) } >Evolve mlindicator and set</Button>
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
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.resetPredictorLSTM(e, this.props) } >Reset predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => this.evolvePredictorLSTM(e, this.props) } >Evolve predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => this.evolveAndSetPredictorLSTM(e, this.props) } >Evolve predictor lstm and set</Button>
          </NavItem>
        </Nav>
       </Navbar>
      </div>
    );
  }
}

export default IclijEvolveBar;
