import React, { PureComponent } from 'react';

import { ServiceParam, ServiceResult } from '../../types/main'
import { Client, ConvertToSelect } from '../util'
import Select from 'react-select';
import { DropdownButton, MenuItem, ButtonToolbar, Nav, Navbar, NavItem, FormControl } from 'react-bootstrap';

class TreeView extends PureComponent {
  constructor(props) {
    super(props);
  }

  getinput(type, key, value) {
    if (type == "java.lang.Boolean") {
        return (<input type="checkbox" defaultChecked={value} onChange={e => this.handleCheckChange(e, key, this.props)}/>);
  }
  if (value != null) {
  return (<input type="text" onChange={e => this.handleChange(e, key, this.props)} defaultValue={value}/>);
  }
}

    handleCheckChange(event, key, props) {
    console.log(key)
    console.log(event);
    console.log(event.target.checked);
    console.log(props);
    //this.props.setmarket({market: event.value});
    props.setconfigvaluemap([ key, event.target.checked ]);
  }
  
    handleChange(event, key, props) {
    console.log(event);
    console.log(event.target.value);
    console.log(event.value);
    console.log(props);
    //this.props.setmarket({market: event.value});
    props.setconfigvaluemap([ key, event.target.value ]);
    //props.setconfigvaluemap({ key: event.target.value });
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

  render() {
    //console.log(this);
    const { main } = this.props;
    //console.log(main);
    const map = this.props.map
  //console.log(map);
  const config = main.config;
  //console.log(config);
  const textMap = config.get('text');
  const typeMap = config.get('type');
  const valueMap = config.get('configValueMap');
  //console.log(typeMap);
  //console.log(textMap);
  const name = map.get('name');
  const text = textMap.get(name);
  const value = valueMap.get(name);
  const type = typeMap.get(name);
  //console.log(name);
  //console.log(value);
  //console.log(type);
  const myinput = this.getinput(type, name, value);
  //console.log(myinput);
    const confMap = map.get('configTreeMap');
    const now = Date.now();
    const map2 = confMap.map((i, j) => this.getview(i, j, now));
    const map3 = Array.from(map2.values());
    if (name == "predictors[@enable]") {
    console.log(valueMap.get("predictors[@enable]"));
    console.log(valueMap.get("predictors.lstm.horizon"));
    }
  return(
  <div>{myinput}{text}({name})
	<ul>
	   { map3 }
	</ul>
  </div>
  )
  }
}

export default TreeView;