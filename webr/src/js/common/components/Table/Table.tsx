/* eslint-disable no-undef */
import React, { memo, useEffect, useMemo } from 'react';
import { useTable, usePagination } from 'react-table'
import { Tooltip } from "react-tooltip";
import styled from 'styled-components'

const Styles = styled.div`
  padding: 1rem;
  table {
    border-spacing: 0;
    border: 1px solid black;
    tr {
      :last-child {
        td {
          border-bottom: 0;
        }
      }
    }
    th,
    td {
      margin: 0;
      padding: 0.5rem;
      border-bottom: 1px solid black;
      border-right: 1px solid black;
      :last-child {
        border-right: 0;
      }
    }
  }
  .pagination {
    padding: 0.5rem;
  }
`

function Table({ columns, data }) {
    console.log("callme");
    console.log(columns);
    console.log(data);
    if (columns == null || data == null || columns.length == 0 || data.length == 0) {
	console.log("callmein");
	return (
	    <h2>No table</h2>
	);
    }
    const hcolumns = useMemo( () => columns, []); //, [mycolumns] );
    const hdata = useMemo( () => data, []);//, [mydata] );
    const {
	getTableProps,
	getTableBodyProps,
	headerGroups,
	prepareRow,
	page, // Instead of using 'rows', we'll use page,
	// which has only the rows for the active page

	// The rest of these things are super handy, too ;)
	canPreviousPage,
	canNextPage,
	pageOptions,
	pageCount,
	gotoPage,
	nextPage,
	previousPage,
	setPageSize,
	state: { pageIndex, pageSize },
    } = useTable({ columns: hcolumns, data: hdata,     initialState: { pageIndex: 0 }, }, usePagination );
    useEffect(() => {
        //Tooltip.rebuild()
    });
    //console.log("callme");
    return (
	<Styles>
	    <div>
		<pre>
		    <code>
			{JSON.stringify(
			    {
				pageIndex,
				pageSize,
				pageCount,
				canNextPage,
				canPreviousPage,
			    },
			    null,
			    2
			)}
		    </code>
		</pre>
		<table {...getTableProps()}>
		    <thead>
			{headerGroups.map(headerGroup => (
			    <tr {...headerGroup.getHeaderGroupProps()}>
				{headerGroup.headers.map(column => (
				    <th {...column.getHeaderProps()}>{column.render('Header')}</th>
				))}
			    </tr>
			))}
		    </thead>
		    <tbody {...getTableBodyProps()}>
			{page.map((row, i) => {
			    prepareRow(row)
			    return (
				<tr {...row.getRowProps()}>
				    {row.cells.map(cell => {
					return <td {...cell.getCellProps()}>{cell.render('Cell')}</td>
				    })}
				</tr>
			    )
			})}
		    </tbody>
		</table>
		{/*
		    Pagination can be built however you'd like.
		    This is just a very basic UI implementation:
		 */}
		<div className="pagination">
		    <button onClick={() => gotoPage(0)} disabled={!canPreviousPage}>
			{'<<'}
		    </button>{' '}
		    <button onClick={() => previousPage()} disabled={!canPreviousPage}>
			{'<'}
		    </button>{' '}
		    <button onClick={() => nextPage()} disabled={!canNextPage}>
			{'>'}
		    </button>{' '}
		    <button onClick={() => gotoPage(pageCount - 1)} disabled={!canNextPage}>
			{'>>'}
		    </button>{' '}
		    <span>
			Page{' '}
			<strong>
			    {pageIndex + 1} of {pageOptions.length}
			</strong>{' '}
		    </span>
		    <span>
			| Go to page:{' '}
			<input
			    type="number"
			    defaultValue={pageIndex + 1}
			    onChange={e => {
				const page = e.target.value ? Number(e.target.value) - 1 : 0
				gotoPage(page)
			    }}
			    style={{ width: '100px' }}
			/>
		    </span>{' '}
		    <select
			value={pageSize}
			onChange={e => {
			    setPageSize(Number(e.target.value))
			}}
		    >
			{[10, 20, 30, 40, 50].map(pageSize => (
			    <option key={pageSize} value={pageSize}>
				Show {pageSize}
			    </option>
			))}
		    </select>
		</div>
	    </div>
	</Styles>
    );
}

export default memo(Table);
