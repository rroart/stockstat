/* eslint-disable no-undef */

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

const fetchApi = {
    search(query, serviceparam) {
        console.log(query);
        console.log(JSON.stringify(serviceparam));
        return fetch(`http://localhost:12345` + query, {
            method: "POST",
            headers: { 'Accept': 'application/json;charset=utf-8', 'Content-Typ\e': 'application/json', },
            body: JSON.stringify(serviceparam),
        })
            .then(statusHelper)
            .then(parseJSON)
            .catch((error) => console.log(error.message))
            .then (data => data)
    }

    export const geturl = (query) => {
	return "http://" + getHost() + ":" + getPort() + query;
    }

}
