/* eslint-disable no-undef */
import React, { PureComponent } from 'react';
import 'react-bootstrap-table-next/dist/react-bootstrap-table2.min.css';
import BootstrapTable from 'react-bootstrap-table-next';

function convert(resultitemtable) {
    console.log("here");
    const array = resultitemtable.rows;
    console.log(array);
    const head = array[0];
    const rest = array.slice(1);
    const columns = [];
    const result = [];
    for(var i = 0; i < head.cols.length; i++) {
	columns.push({ dataField: head.cols[i], text: head.cols[i]});
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

function convert2(array) {
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

function getTable(resultitemtable) {
  return convert(resultitemtable);
}
function getTab(list) {
    const tables = [];
    for(var i = 0; i < list.length; i++) {
	const resultitemtable = list[i];
	const table = getTable(resultitemtable);
	tables.push(table)
    }
    return(
    <div>
    	  { tables.map(item => item) }
	  </div>
    );
}

const MyTable = { convert, convert2, getTab };
export default MyTable;
