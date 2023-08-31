import React, { PureComponent } from 'react';

import { Client, Config, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-date-picker';
import { memo, useCallback, useEffect, useMemo, useState } from "react";

function IclijEvolveBar( { props } ) {
  const [ param, setParam ] = useState(null);

  function resetRecommender(event, props) {

  }

  function resetMLMACD(event, props) {
    props.setconfigvaluemap([ 'aggregators.mlmacd.mlconfig', null ]);
  }

  function resetMlindicator(event, props) {
    props.setconfigvaluemap([ 'aggregators.indicator.mlconfig', null ]);
  }

  function resetPredictorLSTM(event, props) {
    props.setconfigvaluemap([ 'machinelearning.tensorflow.lstm.config', null ]);
  }

  function evolveRecommender(event, props) {
    props.getevolve(['getevolverecommender', false, props.main.config, '']);
  }

  function evolveMLMACD(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'mlmacd']);
  }

  function evolveMlindicator(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'mlindicator']);
  }

  function evolvePredictorLSTM(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'predictorlstm']);
  }

  function evolveAndSetRecommender(event, props) {
    props.getevolve(['getevolverecommender', true, props.main.config, '']);
  }

  function evolveAndSetMLMACD(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'mlmacd']);
  }

  function evolveAndSetMlindicator(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'mlindicator']);
  }

  function evolveAndSetPredictorLSTM(event, props) {
    props.getevolve(['getevolvenn', true, props.main.config, 'predictorlstm']);
  }

  useEffect(() => {
    if (param === undefined || param == null) {
      return;
    }
    const result = Client.fetchApi.search("/" + param.webpath, param);
    result.then(function(result) {
      const list = result.list;
      console.log(result);
      console.log(list);
      const baseurl = Client.geturl("/");
      if (param.async === true) {
        callbackAsync(result.uuid);
      } else {
        const tables = MyTable.getTabNew(result.list, Date.now(), callbackNewTab, props);
        callbackNewTab(tables);
      }
    });
  }, [param]);

  const { main } = props;
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
         <Nav>
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => resetRecommender(e, props) } >Reset recommender</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ e => evolveRecommender(e, props) } >Evolve recommender</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetRecommender(e, props) } >Evolve recommender and set</Button>
          </NavItem>
        </Nav>
       </Navbar>
      <Navbar>
          <Nav>
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => resetMLMACD(e, props) } >Reset MLMACD</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => evolveMLMACD(e, props) } >Evolve MLMACD</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMLMACD(e, props) } >Evolve MLMACD and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
      <Navbar>
         <Nav>
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => resetMlindicator(e, props) } >Reset mlindicator</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => evolveMlindicator(e, props) } >Evolve mlindicator</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMlindicator(e, props) } >Evolve mlindicator and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
      <Navbar>
        <Nav>
          <NavItem eventKey={1} href="#">
	    <Button bsStyle="primary" onClick={ (e) => resetPredictorLSTM(e, props) } >Reset predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
	    <Button bsStyle="primary" onClick={ (e) => evolvePredictorLSTM(e, props) } >Evolve predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetPredictorLSTM(e, props) } >Evolve predictor lstm and set</Button>
          </NavItem>
        </Nav>
       </Navbar>
      </div>
    );
  }
}

export default memo(IclijEvolveBar);
