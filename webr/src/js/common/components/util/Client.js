/* eslint-disable no-undef */
function search2(query, serviceparam, cb) {
    console.log(JSON.stringify(serviceparam));
    /*
  var bla = fetch(`http://localhost:12345` + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus);
    console.log(bla);
*/
    return fetch(`http://localhost:12345` + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus)
    .then(parseJSON)
    .then(console.log)
    .then(cb)
    .catch((error) => console.log(error.message));
}

function search(query, serviceparam, cb) {
    console.log(JSON.stringify(serviceparam));
    return fetch(`http://localhost:12345` + query, {
      method: "POST",
      headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Type': 'application/json', },
      body: JSON.stringify(serviceparam),
  }).then(checkStatus)
    .then(parseJSON)
    .then(cb)
    .catch((error) => console.log(error.message));
}

function checkStatus(response) {
  if (response.status >= 200 && response.status < 300) {
    return response;
  } else {
    const error = new Error(`HTTP Error ${response.statusText}`);
    error.status = response.statusText;
    error.response = response;
    console.log(error); // eslint-disable-line no-console
    throw error;
  }
}

function parseJSON(response) {
  return response.json();
}

const Client = { search };
export default Client;
