/* eslint-disable no-undef */
function search(query, cb) {
  return fetch(`http://localhost:8080` + query, {
    accept: 'application/json',
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
