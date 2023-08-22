import React from 'react';
import ReactDOM from 'react-dom';
import { createRoot } from 'react-dom/client';
import { fromJS } from 'immutable';
import { createBrowserHistory } from 'history';
import { syncHistoryWithStore } from 'react-router-redux';

import routes from './routes';
import Root from './Root';
import configureStore from './redux/configureStore';

let initialState = {};

// rehydrate initialState for JS app
if (window.__INITIAL_STATE__) {
  initialState = window.__INITIAL_STATE__;

  // Transform into Immutable.js collections,
  // but leave top level keys untouched for Redux
  Object
    .keys(initialState)
    .forEach((key) => {
      initialState[key] = fromJS(initialState[key]);
    });
}

const hashHistory = createBrowserHistory();

const store = configureStore(initialState, hashHistory);

const history = syncHistoryWithStore(hashHistory, store);

const element = document.getElementById('app-container');

const root = createRoot(element);

// Render the React application to the DOM
// Root component is to bootstrap Provider, Router and DevTools

root.render(
    <Root history={history} routes={routes} store={store} />
);
