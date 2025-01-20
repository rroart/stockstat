import React, { Component, PureComponent, Fragment } from 'react';

import { Button } from 'react-bootstrap';

import './Main.css';
//import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import { Tabs, Tab } from 'react-bootstrap';

import { Market } from '../Market'
import { Configuration } from '../Configuration'
import { IclijMarket } from '../IclijMarket'
import { IclijConfiguration } from '../IclijConfiguration'
import { ControlPanel } from '../ControlPanel'
import { Test } from '../test'
//import Misc from '../util'
//import Client from '../util/Client'
import { memo, useCallback, useEffect, useMemo, useState } from "react";

const tablist = [];

function newtab() {   console.log("bla3")
//main.watchNewTabMainnn
}

function Main( {props} ) {
  console.log("here" + Object.keys(props));
  const [ tabs, setTabs ] = useState([]);
  function callbackNewTab(data) {
    tabs.push(data);
    setTabs([...tabs]);
    console.log("callb", tabs.length);
    //main.tabs = tabs;
  }

  function newtab2() {
    console.log("bla4")
    const { main } = props;
    main.watchNewTabMain()
  }

  function newtab3() {
    console.log("bla4")
    //const { main } = props;
    console.log(this);
    console.log(props);
    //console.log(props.updateR3());
    //var me = props.updateR3();
    //console.log(me)
    console.log("bla5");
    props.newtabMain(['bla']);
  }

  function onIncrementAsync() { props.incrementasync() }
 function  onIncrement() { props.increment() }
  function onIncrement2() { props.increment2() }

  function getanewtab(data, num) {
    return (
      <Tab key={num} eventKey={num} title="Result">
        {data}
      </Tab>
    )
  }

  const { main } = props;
    console.log(main);
    console.log(Object.keys(main));
    const result = main && main.result2 ? main.result2 : null;
    const result3 = main && main.result3 ? main.result3 : null;
    const count = main && main.count ? main.count : null;
    //const tabs = main && main.tabs ? main.tabs : null;

var mytabs = tabs;
var map = new Object();
map['title']='tit';
//var newtab = new Tab(map);
 console.log(tabs);
 //tabs.push('mytit');
 var arrayLength = tabs.length;
	  console.log("arr");
	  console.log(tabs);
	  console.log(arrayLength);
	  console.log(Object.keys(main));
	  console.log(result);
	  for (var i = 0; i < arrayLength; i++) {
    	  //alert(myStringArray[i]);
    //Do something
}
var nums = [ [ '1' , '2'], ['3' , '4'], ['5', '6']];
    if (result /*&& result.size && result.size > 0*/) {
      return (
        <Fragment>
          <h1>Stockstat</h1>
	  <h2>H{result3}H{count}H</h2>
        <Tabs defaultActiveKey={1} id="maintabs">
          <Tab eventKey={1} title="Market">
            <h2>Market</h2>
            <Market props = { props } callbackNewTab = {callbackNewTab} />
          </Tab>
          <Tab eventKey={2} title="Configuration">
            <h2>Configuration</h2>
	    <Configuration props = { props } config = {main.config} configname = "config"/>
          </Tab>
          <Tab eventKey={3} title="Market">
            <h2>Iclij Market</h2>
            <IclijMarket props = { props } callbackNewTab = {callbackNewTab} />
          </Tab>
          <Tab eventKey={4} title="Configuration">
            <h2>Iclij Configuration</h2>
	    <Configuration props = { props } config = {main.iconfig} configname = "iconfig"/>
          </Tab>
          <Tab eventKey={5} title="Control Panel">
              <h2>Control Panel</h2>
	      <ControlPanel /* TODO props = { props }*/ />
          </Tab>
	  { mytabs.map((item, index) => getanewtab(item, 6 + index)) }
        </Tabs>
        <Button
       onClick={
         () => { newtab3() }
	 }
     >
     New tab
     </Button>
        <Button
       onClick={
         () => { onIncrementAsync() }
       }
     >
     Async
     </Button>
        <Button
       onClick={
         () => { onIncrement() }
       }
     >
     Inc
     </Button>
        <Button
       onClick={
         () => { onIncrement2() }
       }
     >
     Inc2
     </Button>
        <div className="mainOutput">
          <p>If you see this screen, it means you are all setup \o/</p>
          <p>The following JSON are showing contents coming from Redux, Saga and Config.</p>
          <pre>
            {JSON.stringify(result/*.toJS()*/, undefined, 2)}
          </pre>
	  <h4>Affero GPL</h4>
</div>
        </Fragment>
      );
    }
    return <div />;
  }

export default memo(Main);
