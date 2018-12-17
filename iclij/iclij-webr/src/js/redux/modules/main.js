import { createAction, handleActions } from 'redux-actions';
import { Map } from 'immutable';

import type { mainType } from '../../common/types/main'
import { Tabs, Tab } from 'react-bootstrap';

const GET_MAIN = 'app/main/GET_MAIN';
const GET_R3 = 'app/main/GET_R3';
const GET_R4 = 'app/main/GET_R4';
const UPDATE_MAIN = 'app/main/UPDATE_MAIN';
const UPDATE_R3 = 'app/main/UPDATE_R3';
const UPDATE_R4 = 'app/main/UPDATE_R4';
const NEWTAB_MAIN = 'app/main/NEWTAB_MAIN';
const NEWTAB_MAIN3 = 'app/main/NEWTAB_MAIN3';
const INCREMENT = 'app/main/INCREMENT';
const INCREMENT_ASYNC = 'app/main/INCREMENT_ASYNC';
const INCREMENT2 = 'app/main/INCREMENT2';
const GET_COUNT = 'app/main/GET_COUNT';

export const constants = {
  INCREMENT2,
  INCREMENT,
  INCREMENT_ASYNC,
  GET_MAIN,
  GET_R3,
  GET_R4,
  UPDATE_MAIN,
  UPDATE_R3,
  UPDATE_R4,
    NEWTAB_MAIN,
    NEWTAB_MAIN3,
    GET_COUNT,
};

// ------------------------------------
// Actions
// ------------------------------------
export const getAwesomeCode = createAction(GET_MAIN, () => ({}));
export const getAwesomeR3 = createAction(GET_R3, () => ({}));
export const getAwesomeR4 = createAction(GET_R4, () => ({}));
export const updateMain = createAction(UPDATE_MAIN, (result2 : mainType) => ({ result2 }));
export const updateR3 = createAction(UPDATE_R3, (result3 : string) => ({ result3 }));
export const updateR4 = createAction(UPDATE_R4, (result4 : string) => ({ result4 }));
export const newtabMain3 = createAction(NEWTAB_MAIN3, () => ( new Tab()));
export const newtabMain = createAction(NEWTAB_MAIN, (par) => ( par ) );
//export const increment = createAction(INCREMENT);
export const increment = createAction(INCREMENT, ( num = 1) => ({ num }));
export const increment2 = createAction(INCREMENT2, ( count ) => ({ count }));
export const incrementasync = createAction(INCREMENT_ASYNC, () => ({  }));
export const getCount = createAction(GET_COUNT, () => ({ }));
				      
export const actions = {
  getAwesomeCode,
  getAwesomeR3,
  getAwesomeR4,
  updateMain,
  updateR3,
  updateR4,
    newtabMain3,
    newtabMain,
    increment,
    increment2,
    incrementasync,
    getCount,
};

export const reducers = {
  [UPDATE_MAIN]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
  [UPDATE_R3]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
  [UPDATE_R4]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
    [NEWTAB_MAIN3]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
    [NEWTAB_MAIN]: (state, { payload }) =>
    state.set({
	tabs: gettabs4(state, payload)
	})
	//console.log('ppp')
	//console.log(payload)
	//const newArr = state.get('tabs').concat([payload])  
        //const idPositions = newArr.map(el => el.id)
        //const newPayload = newArr.filter((item, pos, arr) => {
        //return idPositions.indexOf(item.id) == pos;
         //                     })
	//return state.merge({ payload: newPayload })
    //}
	,
    [INCREMENT]: (state, { payload }) =>
	state.merge({
      count: state.get('count') + 1
    }),
    [INCREMENT2]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
  [INCREMENT_ASYNC]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
  [GET_COUNT]: (state, { payload }) =>
    state.merge({
      ...payload,
    }),
}

function gettabs(state) {
    console.log("state0");
    console.log(state);
    var arr = (state.get('tabs'));
    var arrayLength = arr.length;
    arr.push('newTab'+arrayLength);
    console.log("state1");
    console.log(state);
    return arr;
}

function gettabs4(state, payload) {
    console.log("state0");
    console.log(state);
    var arr = (state.get('tabs'));
    console.log(arr);
    var arrayLength = arr.length;
    var newpay = payload + arrayLength;
    arr.push(newpay);
    console.log("state1");
    console.log(state);
    console.log(arr);
    return arr;
}

function gettabs2(state, payload) {
    var tabs = []
    console.log("state0");
    console.log(state);
    var arr = (state.get('tabs'));
    console.log(arr);
    var arrayLength = arr.length;
    arr.push('newTab'+arrayLength);
    console.log("state1");
    console.log(state);
    return arr;
}

function gettabs3(state) {
    var tabs = []
    console.log("state0");
    console.log(state);
    var arr = (state.get('tabs'));
    console.log(arr);
    var arrayLength = arr.length;
    //arr.push('newTab'+arrayLength);
    console.log("state1");
    console.log(state);
    return 'newTab'+arrayLength;
    //return arr;
}

export const initialState = () =>
  Map({
    result2: '',
    result3: '',
    result4: '',
      tabs: [],
      count: 0,
  })

export default handleActions(reducers, initialState());
