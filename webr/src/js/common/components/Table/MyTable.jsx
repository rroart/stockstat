/* eslint-disable no-undef */
import React, { PureComponent } from 'react';
import 'react-bootstrap-table-next/dist/react-bootstrap-table2.min.css';
import BootstrapTable from 'react-bootstrap-table-next';
import ReactTable from "react-table";
import 'react-table/react-table.css';

function convert(resultitemtable, date) {
    console.log("here");
    const array = resultitemtable.rows;
    console.log(array);
    const head = array[0];
    const rest = array.slice(1);
    const columns = [];
    const result = [];
    for(var i = 0; i < head.cols.length; i++) {
	columns.push({ accessor: head.cols[i], Header: head.cols[i], sort: true });
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
    console.log(columns);
    return (
    <ReactTable key={date} data={ result } columns={ columns } />
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

function getTable(resultitemtable, date) {
  return convert(resultitemtable, date);
}
function getTab(list, date) {
    const tables = [];
    for(var i = 0; i < list.length; i++) {
	const resultitemtable = list[i];
	const table = getTable(resultitemtable, date + i);
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
