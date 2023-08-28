import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';
import TreeView from './TreeView';
import { MyMap } from '../util'

function ConfigTree( { props, config, configname } ) {
  function  getview(value, key, date) {
    const mykey = date + key;
    return(
      <li key={mykey}>
        <TreeView props = {props} config = {config} map={value} configname = { configname } />
      </li>
    )
  }

  const { main } = props;
  var configTreeMap = config && MyMap.myget(config, 'configTreeMap') ? MyMap.myget(config, 'configTreeMap') : new Map();
  if (configTreeMap === null) {
    console.log("ccccceeeee");
    configTreeMap = new Map();
  }
  //console.log(configTreeMap);
  const confMap = MyMap.myhas(configTreeMap, 'configTreeMap') ? MyMap.myget(configTreeMap, 'configTreeMap') : [];
  const now = Date.now();
  const map2 = MyMap.mymap(confMap);
  const itemlist = [];
  for (let [key, value] of map2) {
    itemlist.push(getview(value, key, now));
  }
  const map3 = itemlist; // Array.from(itemlist);
  return(
    <div>
      <h2>Config tree root</h2>
      <ul>
        { map3 }
      </ul>
    </div>
  )
}

export default ConfigTree;
