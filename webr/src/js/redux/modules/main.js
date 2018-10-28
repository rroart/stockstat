import { createAction, handleActions } from 'redux-actions';
import { Map } from 'immutable';

import type { mainType } from '../../common/types/main'
import { Tabs, Tab } from 'react-bootstrap';
import Immutable from 'immutable'

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
const SETMARKET = 'app/main/SETMARKET';
const SETMARKETS = 'app/main/SETMARKETS';
const GETMARKETS = 'app/main/GETMARKETS';
const SETSTARTDATE = 'app/main/SETSTARTDATE';
const SETENDDATE = 'app/main/SETENDDATE';
const SETCONFIG = 'app/main/SETCONFIG';
const SETCONFIGVALUE = 'app/main/SETCONFIGVALUE';
const SETCONFIGVALUEMAP = 'app/main/SETCONFIGVALUEMAP';
const GETCONFIG = 'app/main/GETCONFIG';
const GETCONTENT = 'app/main/GETCONTENT';
const GETEVOLVERECOMMENDER = 'app/main/GETEVOLVERECOMMENDER';
const GETEVOLVENN = 'app/main/GETEVOLVENN';
const GETEVOLVE = 'app/main/GETEVOLVE';

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
    SETMARKET,
    SETMARKETS,
    GETMARKETS,
    SETSTARTDATE,
    SETENDDATE,
    SETCONFIG,
    SETCONFIGVALUE,
    SETCONFIGVALUEMAP,
    GETCONFIG,
    GETCONTENT,
    GETEVOLVERECOMMENDER,
    GETEVOLVENN,
    GETEVOLVE,
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
export const setmarket = createAction(SETMARKET, (market) => ({ market } ) );
export const getMarkets = createAction(GETMARKETS, () => ( {} ) );
export const setmarkets = createAction(SETMARKETS, (markets) => ( { markets } ) );
export const setstartdate = createAction(SETSTARTDATE, (startdate) => ( { startdate } ) );
export const setenddate = createAction(SETENDDATE, (enddate) => ( { enddate } ) );
export const setconfig = createAction(SETCONFIG, (config) => ( { config } ) );
export const setconfigvalue = createAction(SETCONFIGVALUE, ( array ) => ( array ) );
export const setconfigvaluemap = createAction(SETCONFIGVALUEMAP, ( array ) => ( array ) );
export const getConfig = createAction(GETCONFIG, () => ( {} ) );
export const getcontent = createAction(GETCONTENT, (config) => ( { config } ) );
export const getevolverecommender = createAction(GETEVOLVERECOMMENDER, () => ( {} ) );
export const getevolvenn = createAction(GETEVOLVENN, () => ( {} ) );
export const getevolve = createAction(GETEVOLVE, (array) => ( { array } ) );
				      
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
    setmarket,
    setmarkets,
    setstartdate,
    setenddate,
    setconfig,
    setconfigvalue,
    setconfigvaluemap,
    getConfig,
    getMarkets,
    getcontent,
    getevolverecommender,
    getevolvenn,
    getevolve,
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
	//state.merge({tabs: state.get('tabs').push(payload)})
	//state.merge({tabs: [].concat(state.get('tabs'), [ payload ])})
	state.set({
	    'tabs': gettabs4(state, payload)
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
    [SETMARKET]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETMARKETS]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETSTARTDATE]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETENDDATE]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETCONFIG]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETCONFIGVALUE]: (state, { payload }) =>
	state.merge({
	    config: getConfigAfterSet(state, payload)
    }),
    [SETCONFIGVALUEMAP]: (state, { payload }) =>
	state.merge({
	    config: getConfigValueMapAfterSet(state, payload)
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
    arr.push(payload);
    console.log("state1");
    console.log(state);
    console.log(arr);
    return arr;
}

function gettabs4not(state, payload) {
    console.log("state0");
    console.log(state);
    var st2 = ([ ...state, payload]);
    console.log(st2);
    var st3 = {
		...state,
		markets: payload
    };
    console.log(st3);
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

function gettabs5(state, payload) {
    console.log("state0");
    console.log(state);
    var st2 = ([ ...state, payload]);
    console.log(st2);
    var st3 = {
		...state,
		markets: payload
    };
    console.log(st3);
    var arr = (state.get('tabs'));
    console.log(arr);
    var arrayLength = arr.length;
    var newpay = payload + arrayLength;
    arr.push(newpay);
    console.log("state1");
    console.log(state);
    console.log(arr);
    return new Immutable.Map(state);
}

function gettabs6(state, payload) {
    console.log("state0");
    console.log(state);
    var st2 = ([ ...state, payload]);
    console.log(st2);
    var st3 = {
		...state,
		markets: payload
    };
    console.log(st3);
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

function getConfigAfterSet(state, payload) {
    //state.get('config').set(payload)
    var config = state.get('config');
    //console.log(config);
    //console.log(payload);
    //var valueMap = config.get('configValueMap');
    //console.log(valueMap);
    //var valueMap2 = valueMap; //.set(payload);
    //var k = Object.keys(payload)[0];
    //var v = Object.values(payload)[0];
    //valueMap2 = valueMap2.set(k, v);
    //valueMap2 = valueMap2.set({k: v});
    //console.log(k);
    //console.log(v);
    //console.log(valueMap2.get(payload));
    //console.log(valueMap2.get(k));
    //console.log(valueMap2.get("predictors[@enable]"));
    //console.log(valueMap2.get("predictors.lstm.horizon"));
    //console.log(valueMap);
    //console.log(valueMap2);
    return config.set(payload[0], payload[1]);
}

function getConfigValueMapAfterSet(state, payload) {
    //state.get('config').set(payload)
    var config = state.get('config');
    //console.log(config);
    //console.log(payload);
    //var valueMap = config.get('configValueMap');
    //console.log(valueMap);
    //var valueMap2 = valueMap; //.set(payload);
    //var k = Object.keys(payload)[0];
    //var v = Object.values(payload)[0];
    //valueMap2 = valueMap2.set(k, v);
    //valueMap2 = valueMap2.set({k: v});
    //console.log(k);
    //console.log(v);
    //console.log(valueMap2.get(payload));
    //console.log(valueMap2.get(k));
    //console.log(valueMap2.get("predictors[@enable]"));
    //console.log(valueMap2.get("predictors.lstm.horizon"));
    //console.log(valueMap);
    //console.log(valueMap2);
    const valueMap = config.get('configValueMap');
    return config.set('configValueMap', valueMap.set(payload[0], payload[1]));
}

export const initialState = () =>
  Map({
    result2: '',
    result3: '',
    result4: '',
      tabs: [],
      count: 0,
      startdate: '',
      enddate: '',
      market: '',
      markets: [],
      config: '',
  })

export default handleActions(reducers, initialState());
