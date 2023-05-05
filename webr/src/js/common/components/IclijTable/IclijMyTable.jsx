/* eslint-disable no-undef */
import React, { PureComponent } from 'react';
import ReactTable from "react-table";
import 'react-table/react-table.css';

function handleButtonClick(props, e, value) {
    console.log("hhaha");
    console.log(e);
    console.log(value);
    console.log(props);
    props.getcontentgraph(props.main.config, value, props);
}

function convert(list, date, props) {
    console.log("here");
    console.log(list);
    const array = list.list;
    console.log(array);
    if (array === undefined || array.length == 0) {
	return (
	    <div>
	    <h3>{ list.title }</h3>
	    </div>
	);
    }
    const head = Object.keys(array[0]);
    const rest = array
    const columns = [];
    const result = [];
    for(var i = 0; i < head.length; i++) {
	if (head[i] == "Img") {
	    //columns.push({ accessor: head[i], Header: head[i], sort: true, id: 'button', Cell: ({value}) => (<a onClick={console.log('clicked value', value)}>Button</a>) });
	    columns.push({ accessor: head[i], Header: head[i], sort: true, id: 'button', Cell: ({value}) => (<a onClick={(e) => handleButtonClick(props, e, value)}>{value}</a>) });
	} else {
	    columns.push({ accessor: head[i], Header: head[i], sort: true, Cell: ({value}) => { if (typeof value === "boolean") { return String(value) } else { return value; } } });
	}
    }
    console.log(columns);
    console.log(head);
    console.log(head.length);
    console.log(rest);
    console.log(rest.length);
    for(var j = 0; j < rest.length; j++) {
	const row = rest[j];
	if (j == 0) {
	    console.log(row);
	    console.log(head[0]);
	    console.log(head[1]);
	}
	const newrow = [];
	for(var i = 0; i < head.length; i++) {
	    newrow[head[i]] = row[head[i]];
	}
	if (j == 0) {
	    console.log(newrow);
	}
	/*
	if (head[0] == "Img") {
	    newrow[head[0]] = "bla";
	}
	*/
	result.push(newrow);
    }
    console.log(result);
    console.log(columns);
    return (
	<div>
    <h3>>{ list.title }</h3>
    <ReactTable key={date} data={ result } columns={ columns } />
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
    for(var i = 0; i < head.length; i++) {
	columns.push({ dataField: head[i], text: head[i], sort: true });
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

function image(data, i) {
    const Example = ({ data }) => <img src={`data:image/svg+xml;base64,${data}`} width="1000" height="800"/>;
    return (
	<Example key={i} data={data} />
    );
}

function getTab(lists, date, props) {
    const tables = [];
    for(var i = 0; i < lists.length; i++) {
	console.log(lists[i])
	const list = lists[i];
	const table = getTable(list, date + i, props);
	tables.push(table);
	console.log(table);
    }
    return(
    <div>
    	  { tables.map(item => item) }
	  </div>
    );
}

const IclijMyTable = { convert, convert2, getTab };
export default IclijMyTable;
