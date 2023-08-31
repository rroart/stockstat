import React, { PureComponent } from 'react';

import {Client, Config, ConvertToSelect} from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Button, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import { ServiceParam, ServiceResult } from '../../types/main'
import DatePicker from 'react-16-bootstrap-date-picker';
import { memo, useCallback, useEffect, useMemo, useState } from "react";
import { ServiceParam, ServiceResult, NeuralNetCommand, IclijServiceParam, IclijServiceResult } from '../../types/main'

function EvolveBar( { props, callbackNewTab }) {
  const [ param, setParam ] = useState(null);
  const [ uuids, setUuids ] = useState( new Set() );

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
    const param = Config.getParam(main.props.config, "/getevolvenn");
    param.ids = new Set(['mlmacd']);
    setParam(param);
  }

  function evolveMlindicator(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'mlindicator']);
  }

  function evolvePredictorLSTM(event, props) {
    props.getevolve(['getevolvenn', false, props.main.config, 'predictorlstm']);
  }

  function  evolveAndSetRecommender(event, props, set) {
    props.getevolve(['getevolverecommender', true, props.main.config, '']);
  }

  function evolveAndSetMLMACD(event, props, set) {
    const param = Config.getParam(main.props.iconfig, "/getevolvenn");
    param.ids = new Set(['mlmacd']);
    setParam(param);
  }

  function evolveAndSetMlindicator(event, props, set) {
    props.getevolve(['getevolvenn', true, props.main.config, 'mlindicator']);
  }

  function evolveAndSetPredictorLSTM(event, props, set) {
    const param = Config.getParam(main.props.iconfig, "/getcontent");
    setParam(param);
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
        const update = result.get("maps").get("update");
        for (const [key, value] of Object.entries(update)) {
          mainActions.setconfigvaluemap([key, value]);
        }
        const tables = MyTable.getTabNew(result.list, Date.now(), callbackNewTab, props);
        callbackNewTab(tables);
      }
    });
  }, [param]);

  const callbackAsync = useCallback( (uuid) => {
    uuids.push(uuid);
    setUuids([...uuids]);
  }, [uuids]);

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
            <Button bsStyle="primary" onClick={ e => evolveAndSetRecommender(e, props, false) } >Evolve recommender</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetRecommender(e, props, true) } >Evolve recommender and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
      <Navbar>
        <Nav>
          <NavItem eventKey={1} href="#">
            <Button bsStyle="primary" onClick={ (e) => resetMLMACD(e, props) } >Reset MLMACD</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMLMACD(e, props, false) } >Evolve MLMACD</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMLMACD(e, props, true) } >Evolve MLMACD and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
      <Navbar>
        <Nav>
          <NavItem eventKey={1} href="#">
            <Button bsStyle="primary" onClick={ (e) => resetMlindicator(e, props) } >Reset mlindicator</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMlindicator(e, props, false) } >Evolve mlindicator</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetMlindicator(e, props, true) } >Evolve mlindicator and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
      <Navbar>
        <Nav>
          <NavItem eventKey={1} href="#">
            <Button bsStyle="primary" onClick={ (e) => resetPredictorLSTM(e, props) } >Reset predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={2} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetPredictorLSTM(e, props, false) } >Evolve predictor lstm</Button>
          </NavItem>
          <NavItem eventKey={3} href="#">
            <Button bsStyle="primary" onClick={ (e) => evolveAndSetPredictorLSTM(e, props, true) } >Evolve predictor lstm and set</Button>
          </NavItem>
        </Nav>
      </Navbar>
    </div>
  );
}

export default memo(EvolveBar);
