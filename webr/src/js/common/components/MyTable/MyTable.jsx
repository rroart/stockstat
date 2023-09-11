/* eslint-disable no-undef */
import React, { PureComponent } from 'react';
import { useTable } from 'react-table'
import { Tooltip } from "react-tooltip";
// import 'react-tooltip/dist/react-tooltip.css'
import { Client } from '../util'
import { constants as mainConstants, actions as mainActions } from '../../../redux/modules/main';
import { Table } from '../Table'

function handleButtonClick(value, callback) {
  console.log("here1");
  callback(value);
  if (true) return;
  const result = Client.fetchApi.search("/searchmlt", { str : value });
  result.then(function(result) { callback(result); });
  console.log("here1");
  const htm=(<h2>hei</h2>);
  console.log("call " , htm);
  mainActions.newtabMain(htm);
  console.log("call ", htm);
}

function handleDownload(value, filename) {
  console.log("here2");
  console.log(value);
  console.log(value.original);
  console.log(value.original['Md5/Id']);
  Client.fetchApi.download("/download", { str : value.original['Md5/Id'], filename : filename });
  console.log("here2");
}

function getcolumnsold(resultitemtable, baseurl, callback) {
  const array = resultitemtable.rows;
  console.log(array);
  if (array.length == 0) {
    return [];
  }
  const columns = [];
  const head = array[0];
  for(let i = 0; i <head.cols.length; i++) {
    if (head.cols[i] == "Img") {
      columns.push({ accessor:head.cols[i], Header:head.cols[i], sort: true, id: 'col'+i, Cell: ({value}) => (<a onClick = {(e) => handleButtonClick(value, callback)}>{value}</a>)});
      continue;
    }
    if (head.cols[i] != "Img") {
      columns.push({ accessor: head.cols[i], Header: head.cols[i], sort: true });
      continue;
    }
    if (head[i] != "Score") {
      columns.push({ accessor:head.cols[i], Header:head.cols[i], sort: true, id: 'col'+i, Cell: (row) => (<span data-tip = {row.value}>{row.value}</span>) });
    } else {
      columns.push({ accessor:head.cols[i], Header:head.cols[i], sort: true });
    }
  }
  console.log(columns);
  return columns;
}

function getcolumns(resultitemtable, baseurl, callback) {
  const array = resultitemtable.list;
  console.log(array);
  if (array.length == 0) {
    return [];
  }
  const columns = [];
  const head = array[0];
  console.log(Object.keys(head));
  console.log(head + " " + array);
  const keys = Object.keys(head);
  for(let i = 0; i < keys.length; i++) {
    if (keys[i] == "Md5/Id") {
      columns.push({ accessor: keys[i], Header: keys[i], sort: true, id: 'col'+i, Cell: ({value}) => (<a onClick = {(e) => handleMLT(value, callback)}>{value}</a>)});
      continue;
    }
    if (keys[i] == "Filename") {
      columns.push({ accessor: keys[i], Header: keys[i], sort: true, id: 'col'+i, Cell: ({row, value}) => (<a onClick = { (e) => handleDownload(row, value)}>{value}</a>)});
      continue;
    }
    if (keys[i] != "Score") {
      columns.push({ accessor: keys[i], Header: keys[i], sort: true, id: 'col'+i, Cell: (row) => (<span data-tip = {row.value}>{row.value}</span>) });
    } else {
      columns.push({ accessor: keys[i], Header: keys[i], sort: true });
    }
  }
  console.log(columns);
  return columns;
}

function getdataold(resultitemtable) {
  const array = resultitemtable.rows;
  console.log(array);
  if (array.length == 0) {
    return [];
  }
  const result = [];
  const head = array[0];
  const rest = array.slice(1);
  for(let j = 0; j < rest.length; j++) {
    const row = rest[j];
    console.log(row);
    const newrow = [];
    for(let i = 0; i < head.cols.length; i++) {
      newrow[head.cols[i]] = row.cols[i];
      //console.log(newrow, head.items[i]);
    }
    result.push(newrow);
  }
  console.log(result);
  return result;
}

function getdata(resultitemtable) {
  const array = resultitemtable.list;
  console.log(array);
  if (array.length == 0) {
    return [];
  }
  const result = [];
  const keys = Object.keys(array[0]);
  const rest = array; //.slice(1);
  if (true) return array;
  for(let j = 0; j < rest.length; j++) {
    const row = rest[j];
    console.log(row);
    const newrow = [];
    for(let i = 0; i < keys.length; i++) {
      newrow[keys[i]] = row[keys[i]];
      //console.log(newrow, keys[i]);
    }
    result.push(newrow);
  }
  console.log(result);
  return result;
}

function convert(resultitemtable, date, props) {
  console.log("here");
  console.log(resultitemtable);
  const array = resultitemtable;
  console.log(array);
  if (array.length == 0) {
    return (
      <ReactTable key={date} data={ [] } columns={ [] } />
    );
  }
  const head = array[0];
  const rest = array.slice(1);
  const columns = [];
  const result = [];
  console.log(head);
  console.log(head.items);
  console.log(head.items.length);
  for(var i = 0; i < head.items.length; i++) {
    /*
      if (head.items[i] == "Md5/Id") {
      var obj = row.value;
      console.log(obj);
      var id = obj["id"];
      console.log(id);
      columns.push({ accessor: head.items[i], Header: head.items[i], sort: true, id: 'col'+i, Cell: ({row}) => (<a onClick={(e) => handleClick(props, e, row.value[0])}>{Object.keys(row.value)}</a>) });
      //columns.push({ accessor: head.items[i], Header: head.items[i], sort: true, id: 'col'+i, Cell: ({row}) => (<a onClick={(e) => handleButtonClick(props, e, row.value)}>{row.value}</a>) });
      //columns.push({ accessor: head.cols[i], Header: head.cols[i], sort: true, id: 'button', Cell: ({value}) => (<a onClick={(e) => handleButtonClick(props, e, value)}>{value}</a>) });

      continue;
      }
    */
    if (head.items[i] != "Score") {
      //columns.push({ accessor: head[i], Header: head[i], sort: true, id: 'button', Cell: ({value}) => (<a onClick={console.log('clicked value', value)}>Button</a>) });
      //columns.push({ accessor: head.items[i], Header: head.items[i], sort: true, id: 'button'+i, Cell: (row) => (<div><span title={row.value}>{row.value}</span></div>) });
      columns.push({ accessor: head.items[i], Header: head.items[i], sort: true, id: 'col'+i, Cell: (row) => (<span data-tip = {row.value}>{row.value}</span>) });
      //columns.push({ accessor: head.items[i], Header: head.items[i], sort: true, id: 'button'+i, Cell: (row) => (<span id={head.items[i]} data-tip = {row.value} dangerouslySetInnerHTML={{__html: row.value }}/>) });
    } else {
      columns.push({ accessor: head.items[i], Header: head.items[i], sort: true });
    }
  }
  console.log(columns);
  console.log(head);
  console.log(Object.keys(head));
  console.log(head.length);
  console.log(rest);
  console.log(rest.length);
  for(var j = 0; j < rest.length; j++) {
    const row = rest[j].items;
    console.log(row);
    const newrow = [];
    for(var i = 0; i < head.items.length; i++) {
      //console.log(i);
      newrow[head.items[i]] = row[i];
      console.log(typeof(row[i]))
      //console.log(i);
    }
    /*
      {
      var afilename = newrow["Filename"];
      //newrow["Md5/Id"] = { id : newrow["Md5/Id"], filename : afilename };
      newrow["Md5/Id"] = [ newrow["Md5/Id"], afilename ];
      };
    */
    /*
      if (head[0] == "Img") {
      newrow[head[0]] = "bla";
      }
    */
    //console.log(newrow);
    result.push(newrow);
  }
  console.log(result);
  console.log(columns);
}

function gethtable(getTableProps, getTableBodyProps, headerGroups, rows, prepareRow) {
  return (
    <div>
      <table {...getTableProps()} style={{ border: 'solid 1px blue' }}>
        <thead>
        {headerGroups.map(headerGroup => (
          <tr {...headerGroup.getHeaderGroupProps()}>
            {headerGroup.headers.map(column => (
              <th
                {...column.getHeaderProps()}
                style={{
                  borderBottom: 'solid 3px red',
                  background: 'aliceblue',
                  color: 'black',
                  fontWeight: 'bold',
                }}
              >
                {column.render('Header')}
              </th>
            ))}
          </tr>
        ))}
        </thead>
        <tbody {...getTableBodyProps()}>
        {rows.map(row => {
          prepareRow(row)
          return (
            <tr {...row.getRowProps()}>
              {row.cells.map(cell => {
                return (
                  <td
                    {...cell.getCellProps()}
                    style={{
                      padding: '10px',
                      border: 'solid 1px gray',
                      background: 'papayawhip',
                    }}
                  >
                    {cell.render('Cell')}
                  </td>
                )
              })}
            </tr>
          )
        })}
        </tbody>
      </table>
    </div>
  );
}

function convert2(array) {
  console.log("here");
  console.log(array);
  const head = array[0];
  const rest = array.slice(1);
  const columns = [];
  const result = [];
  for(var i = 0; i < head.length; i++) {
    columns.push({ accessor: head[i], Header: head[i]});
  }
  console.log(columns);
  console.log(head);
  console.log(head.length);
  console.log(rest);
  console.log(rest.length);
  for(var j = 0; j < rest.length; j++) {
    const row = rest[j];
    const newrow = [];
    for(var i = 0; i < head.length; i++) {
      newrow[head[i]] = row[i];
    }
    result.push(newrow);
  }
  console.log(result);
  console.log(columns);
  return (
    <ReactTable data={ result } columns={ columns } />
  );
}

function convertbs(resultitemtable) {
  console.log("here");
  const array = resultitemtable.rows;
  console.log(array);
  const head = array[0];
  const rest = array.slice(1);
  const columns = [];
  const result = [];
  for(var i = 0; i < head.cols.length; i++) {
    columns.push({ dataField: head.cols[i], text: head.cols[i], sort: true });
  }
  console.log(columns);
  console.log(head);
  console.log(head.length);
  console.log(rest);
  console.log(rest.length);
  for(var j = 0; j < rest.length; j++) {
    const row = rest[j];
    //console.log(row);
    const newrow = [];
    for(var i = 0; i < head.cols.length; i++) {
      newrow[head.cols[i]] = row.cols[i];
    }
    result.push(newrow);
  }
  console.log(result);
  return (
    <BootstrapTable keyField='id' data={ result } columns={ columns } />
  );
}

function convert2bs(array) {
  console.log("here");
  console.log(array);
  const head = array[0];
  const rest = array.slice(1);
  const columns = [];
  const result = [];
  for(var i = 0; i < head.length; i++) {
    columns.push({ dataField: head[i], text: head[i]});
  }
  console.log(columns);
  console.log(head);
  console.log(head.length);
  console.log(rest);
  console.log(rest.length);
  for(var j = 0; j < rest.length; j++) {
    const row = rest[j];
    const newrow = [];
    for(var i = 0; i < head.length; i++) {
      newrow[head[i]] = row[i];
    }
    result.push(newrow);
  }
  console.log(result);
  return (
    <BootstrapTable keyField='id' data={ result } columns={ columns } />
  );
}

function getTable(resultitemtable, date, props) {
  return convert(resultitemtable, date, props);
}

function getTableNew(hcolumns, hdata, title) {
  return <div>
    <Tooltip effect="solid" html="true"/>
    <h3>{ title }</h3>
    <Table columns={hcolumns} data={hdata} />
  </div>;
}

function image(data, i) {
  const Example = ({ data }) => <img src={`data:image/svg+xml;base64,${data}`} width="1000" height="800"/>;
  return (
    <Example key={i} data={data} />
  );
}

function getTab(list, date, props) {
  console.log(list);
  const tables = [];
  for(var i = 0; i < list.length; i++) {
    console.log(list[i])
    console.log(list[i]._class)
    if (true || list[i]._class == "roart.model.ResultItemTable") {
      const resultitemtable = list[i];
      const table = getTable(resultitemtable, date + i, props);
      tables.push(table);
      console.log(table);
    }
    if (list[i]._class == "roart.model.ResultItemStream") {
      const stream = list[i];
      //let buff = new Buffer(stream.bytes, 'base64');
      //let text = buff.toString('ascii');
      //console.log(text);
      const example = image(stream.bytes, i);
      tables.push(example);
      console.log(example);
    }
  }
  return(
    <div>
      { tables.map(item => item) }
    </div>
  );
}

function getTabNew(lists, date, callback) {
  const baseurl = Client.geturl("/");
  const tables = [];
  for(let i = 0; i < lists.length; i++) {
    const resultitemtable = lists[i];
    console.log("rrr"+Object.keys(resultitemtable));
    if (resultitemtable.list === undefined) {
      tables.push(<h3>{ resultitemtable.title } </h3>);
      continue;
    }
    const mycolumns = getcolumns(resultitemtable, baseurl, callback);
    const mydata = getdata(resultitemtable);
    const tab = getTableNew(mycolumns, mydata, resultitemtable.title);
    tables.push(tab);
  }
  return(
    <div>
      { tables.map(item => item) }
    </div>
  );
}

function getTabNewOld(lists, date, callback) {
  const baseurl = Client.geturl("/");
  const tables = [];
  console.log("rrrrrrrrrrrrrrrrrrrrr");
  console.log(lists);
  for(let i = 0; i < lists.length; i++) {
    if (lists[i]._class == "roart.model.ResultItemTable") {
      const resultitemtable = lists[i];
      console.log("rrr"+Object.keys(resultitemtable));
      const mycolumns = getcolumnsold(resultitemtable, baseurl, callback);
      const mydata = getdataold(resultitemtable);
      const tab = getTableNew(mycolumns, mydata, resultitemtable.title);
      console.log(mycolumns);
      console.log(mydata);
      tables.push(tab);
    }
    if (lists[i]._class == "roart.model.ResultItemStream") {
      const stream = lists[i];
      //let buff = new Buffer(stream.bytes, 'base64');
      //let text = buff.toString('ascii');
      //console.log(text);
      const example = image(stream.bytes, i);
      tables.push(example);
      console.log(example);
    }
  }
  //console.log(lists.o.o);
  //console.log(Object.keys(lists.o.o));
  //console.log(tables[-1]);
  return(
    <div>
      { tables.map(item => item) }
    </div>
  );
}

const MyTable = { convert, convert2, getTab, getcolumns, getdata, gethtable, getTabNew, getTabNewOld };
export default MyTable;
//export default convert, t;
