/* eslint-disable no-undef */

import { env } from '../../../../env'

function getPort() {
    console.log(env.NODE_ENV);
    if (typeof env.REACT_APP_MYPORT !== 'undefined') {
	return env.REACT_APP_MYPORT;
    }
    return 12345; // 80;
}

function getHost() {
    console.log("pppp");
    console.log(env);
    if (typeof env.REACT_APP_MYSERVER !== 'undefined') {
	return env.REACT_APP_MYSERVER;
    }
    return "localhost";
}

function getIPort() {
    console.log(env.REACT_APP_NODE_ENV);
    if (typeof env.REACT_APP_MYIPORT !== 'undefined') {
        return env.REACT_APP_MYIPORT;
    }
    return 12346; // 80;
}

function getIHost() {
    console.log("pppp");
    console.log(env);
    if (typeof env.REACT_APP_MYISERVER !== 'undefined') {
        return env.REACT_APP_MYISERVER;
    }
    return "localhost";
}

function getAPort() {
    console.log(env.REACT_APP_NODE_ENV);
    if (typeof env.REACT_APP_MYAPORT !== 'undefined') {
        return env.REACT_APP_MYAPORT;
    }
    return 12347; // 80;
}

function getAHost() {
    console.log("pppp");
    console.log(env);
    if (typeof env.REACT_APP_MYASERVER !== 'undefined') {
        return env.REACT_APP_MYASERVER;
    }
    return "localhost";
}

function search(query, serviceparam, cb) {
    console.log(JSON.stringify(serviceparam));
    /*
  var bla = fetch(`http://localhost:22345` + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus);
    console.log(bla);
*/
    console.log("http://" + getHost() + ":" + getPort() + query);
    return fetch("http://" + getHost() + ":" + getPort() + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus)
	.then(parseJSON)
    //.then(console.log)
    .then(cb)
    .catch((error) => console.log(error.message));
}

function searchsynch(query, serviceparam) {
    console.log(JSON.stringify(serviceparam));
    fetch("http://" + getHost() + ":" + getPort() + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus)
    .then(parseJSON)
    .catch((error) => console.log(error.message));
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  } else {
    const error = new Error(`HTTP Error ${response.statusText}`);
    // TODO
    //error.status = response.statusText;
    //error.response = response;
    console.log(error); // eslint-disable-line no-console
    throw error;
  }
}

function parseJSON(response) {
  return response.json();
}

const fetchApi = {
    search(query, serviceparam) {
	console.log(query);
	console.log(JSON.stringify(serviceparam));
        console.log("http://" + getHost() + ":" + getPort() + query);
	return fetch("http://" + getHost() + ":" + getPort() + query, {
	    method: "POST",
	    headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
	    body: JSON.stringify(serviceparam),
	})
	    .then(statusHelper)
	    .then(parseJSON)
	    .catch((error) => console.log(error.message))
	    .then (data => data)
    },
    search2(query, serviceparam) {
	console.log(query);
	console.log(JSON.stringify(serviceparam));
        console.log("http://" + getIHost() + ":" + getIPort() + query);
	return fetch("http://" + getIHost() + ":" + getIPort() + query, {
	    method: "POST",
	    headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
	    body: JSON.stringify(serviceparam),
	})
	    .then(statusHelper)
	    .then(parseJSON)
	    .catch((error) => console.log(error.message))
	    .then (data => data)
    },
    search3(query, serviceparam) {
	console.log(query);
	console.log(JSON.stringify(serviceparam));
        console.log("http://" + getAHost() + ":" + getAPort() + query);
	return fetch("http://" + getAHost() + ":" + getAPort() + query, {
	    method: "POST",
	    headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
	    body: JSON.stringify(serviceparam),
	})
	    .then(statusHelper)
	    .then(parseJSON)
	    .catch((error) => console.log(error.message))
	    .then (data => data)
    }
}

function statusHelper (response) {
  if (response.status >= 200 && response.status < 300) {
    return Promise.resolve(response)
  } else {
    return Promise.reject(new Error(response.statusText))
  }
}

export const geturl = (query) => {
    return "http://" + getAHost() + ":" + getAPort() + query;
}

export const geturl2 = (query) => {
    return "http://" + getIHost() + ":" + getIPort() + query;
}

const Client = { search, searchsynch, fetchApi, geturl, geturl2 };
export default Client;
