import React from 'react';

import { MyMap } from '../util'

function TreeView( { props, config, map, configname } ) {
  function getinput(type, key, value) {
    if (type == "java.lang.Boolean") {
      return (<input type="checkbox" defaultChecked={value} onChange={e => handleCheckChange(e, key, props)}/>);
    }
    if (value != null) {
      return (<input type="text" onChange={e => handleChange(e, key, props)} defaultValue={value}/>);
    }
  }

  function handleCheckChange(event, key, props) {
    if (config == "config") {
      props.setconfigvaluemap([configname, key, event.target.checked]);
    } else {
      props.seticonfigvaluemap([configname, key, event.target.checked]);
    }
  }

  function handleChange(event, key, props) {
    if (config == "config") {
      props.setconfigvaluemap([ configname, key, event.target.value ]);
    } else {
      props.seticonfigvaluemap([configname, key, event.target.checked]);
    }
  }

  function getview(value, key) {
    const mykey = key + value;
    return(
      <li key={mykey}>
        <TreeView props = {props} config = {config} map={value} configname = { configname } />
      </li>
    )
  }

  console.log(props);
  console.log(config);
  console.log(Object.keys(config));
  const configMaps = MyMap.myget(config, 'configMaps');
  console.log(Object.keys(configMaps));
  const textMap = MyMap.myget(configMaps, 'text');
  const typeMap = MyMap.myget(configMaps, 'map');
  const valueMap = MyMap.myget(config, 'configValueMap');
  const name =  MyMap.myget(map, 'name');
  const text =  MyMap.myget(textMap, name);
  const value =  MyMap.myget(valueMap, name);
  const type =  MyMap.myget(typeMap, name);
  const myinput = getinput(type, name, value);
  const confMap =  MyMap.myget(map, 'configTreeMap');
  if (confMap === undefined) {
    console.log("ccccc" + Object.keys(map));
    console.log("ccccc" + (map));
    return;
  }
  const map2 = MyMap.mymap(confMap);
  const itemlist = [];
  for (let [key, value] of Object.entries(map2)) {
    itemlist.push(getview(value, key));
  }
  const map3 = itemlist; // Array.from(itemlist);
  return(
    <div>{myinput}{text}({name})
      <ul>
        { map3 }
      </ul>
    </div>
  )
}

export default TreeView;
