import React, { Component, PureComponent, Fragment } from 'react';

import { Button } from 'react-bootstrap';

import './Main.css';
//import { Tab, Tabs, TabList, TabPanel } from 'react-tabs';
import { Tabs, Tab } from 'react-bootstrap';

import { Market } from '../Market'
import { Configuration } from '../Configuration'
import { ControlPanel } from '../ControlPanel'
import { Misc } from '../Misc'
import { Test } from '../test'
//import Misc from '../util'
//import Client from '../util/Client'

const tablist = [];

function newtab() {   console.log("bla3")
//main.watchNewTabMainnn
}

class Main extends React.Component {
  newtab2() {
    console.log("bla4")
    const { main } = this.props;
    main.watchNewTabMain()
  }

  newtab3() {
    console.log("bla4")
    //const { main } = this.props;
    console.log(this);
    console.log(this.props);
    //console.log(this.props.updateR3());
    //var me = this.props.updateR3();
    //console.log(me)
    console.log("bla5");
    this.props.newtabMain(['bla']);
  }

  onIncrementAsync() { this.props.incrementasync() }
  onIncrement() { this.props.increment() }
  onIncrement2() { this.props.increment2() }

    getanewtab(data, num) {
	return(
	    <Tab key={num} eventKey={num} title="Result">
              { data }
          </Tab>
	  )
}

  render() {
    const { main } = this.props;
    const result = main && main.result2 ? main.result2 : null;
    const result3 = main && main.result3 ? main.result3 : null;
    const count = main && main.count ? main.count : null;
    const tabs = main && main.tabs ? main.tabs : null;

var mytabs = tabs;
var map = new Object();
map['title']='tit';
var newtab = new Tab(map);
 console.log(tabs);
 //tabs.push('mytit');
 var arrayLength = tabs.length;
	  console.log("arr");
	  console.log(tabs);
	  console.log(arrayLength);
	  for (var i = 0; i < arrayLength; i++) {
    	  //alert(myStringArray[i]);
    //Do something
}

    if (result && result.size && result.size > 0) {
      return (
        <Fragment>
          <h1>Stockstat Iclij</h1>
	  <h2>H{result3}H{count}H</h2>
	  <Tabs defaultActiveKey={1} id="maintabs">
          <Tab eventKey={1} title="Market">
            <h2>Market</h2>
            <Market {...this.props}/>
          </Tab>
          <Tab eventKey={2} title="Configuration">
            <h2>Configuration</h2>
            <Configuration {...this.props}/>
          </Tab>
          <Tab eventKey={3} title="Control Panel">
            <h2>Control Panel</h2>
            <ControlPanel {...this.props}/>
          </Tab>
          { mytabs.map((item, index) => this.getanewtab(item, 4 + index)) }
        </Tabs>
        <Button
       bsStyle="primary"
       onClick={
         () => { this.newtab3() }
	 }
     >
     New tab
     </Button>
        <Button
       bsStyle="primary"
       onClick={
         () => { this.onIncrementAsync() }
       }
     >
     Async
     </Button>
        <Button
       bsStyle="primary"
       onClick={
         () => { this.onIncrement() }
       }
     >
     Inc
     </Button>
        <Button
       bsStyle="primary"
       onClick={
         () => { this.onIncrement2() }
       }
     >
     Inc2
     </Button>
        <div className="mainOutput">
          <p>If you see this screen, it means you are all setup \o/</p>
          <p>The following JSON are showing contents coming from Redux, Saga and Config.</p>
          <pre>
            {JSON.stringify(result.toJS(), undefined, 2)}
          </pre>
	  <h4>Affero GPL</h4>
</div>
        </Fragment>
      );
    }
    return <div />;
  }
}

export default Main;
