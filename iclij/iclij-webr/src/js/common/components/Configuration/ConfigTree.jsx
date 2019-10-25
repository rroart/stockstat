import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import TreeView from './TreeView';

class ConfigTree extends PureComponent {
  constructor(props) {
    super(props);
  }

/*
  componentDidMount() {
    this.props.getConfig();
  }
  */

  bla(i) {
  console.log(i);
  }

  bla2(value, key) {
  console.log(value);
  console.log(key);
  }

  getview(value, key, date) {
  //console.log(this.props);
  //console.log(value);
  //console.log(key);
  const mykey = date + key;
  return(
  <li key={mykey}>
  <TreeView {...this.props} map={value}/>
  </li>
  )
  }

  getviewnot(map) {
  return(
  <li>
  <TreeView {...this.props} map={map}/>	
  </li>
  )
  }

  render() {
    const { main } = this.props;
    //console.log(main);
    const config = main && main.config ? main.config : null;
  //console.log(config);
  var configTreeMap = config && config.get('configTreeMap') ? config.get('configTreeMap') : new Map();
    //console.log(configTreeMap);
    if (configTreeMap === null) {
    configTreeMap = new Map();
    }
    //console.log(configTreeMap);
    const confMap = configTreeMap.has('configTreeMap') ? configTreeMap.get('configTreeMap') : [];
    //console.log(confMap);
    //console.log(confMap.values());
    const now = Date.now();
    const map2 = confMap.map((i, j) => this.getview(i, j, now));
    const map3 = Array.from(map2.values());
    //console.log(map3);
    //confMap.forEach(this.bla2);
    //console.log(confMap.values());
    //confMap.values().map(this.bla());
    //console.log(this.props);
    //console.log(confMap.map(x => this.bla(x)));
    return(
      <div>
        <h2>Config tree root</h2>
	<ul>
	   { map3 }
	</ul>
      </div>
      )
  }
}

export default ConfigTree;