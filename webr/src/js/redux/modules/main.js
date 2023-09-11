import { createAction, handleActions } from 'redux-actions';
import { Map } from 'immutable';

import { Tabs, Tab } from 'react-bootstrap';
import Immutable from 'immutable'
import { MyMap } from '../../common/components/util'

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
const SETMARKET2 = 'app/main/SETMARKET2';
const SETMLMARKET = 'app/main/SETMLMARKET';
const SETMARKETS = 'app/main/SETMARKETS';
const GETMARKETS = 'app/main/GETMARKETS';
const SETSTARTDATE = 'app/main/SETSTARTDATE';
const SETENDDATE = 'app/main/SETENDDATE';
const SETCONFIG = 'app/main/SETCONFIG';
const SETCONFIG2 = 'app/main/SETCONFIG2';
const SETCONFIGVALUE = 'app/main/SETCONFIGVALUE';
const SETCONFIGVALUE2 = 'app/main/SETCONFIGVALUE2';
const SETCONFIGVALUEMAP = 'app/main/SETCONFIGVALUEMAP';
const SETICONFIGVALUEMAP = 'app/main/SETICONFIGVALUEMAP';
const GETCONFIG = 'app/main/GETCONFIG';
const GETCONTENT = 'app/main/GETCONTENT';
const GETCONTENT2 = 'app/main/GETCONTENT2';
const GETCONTENTGRAPH = 'app/main/GETCONTENTGRAPH';
const GETEVOLVERECOMMENDER = 'app/main/GETEVOLVERECOMMENDER';
const GETEVOLVENN = 'app/main/GETEVOLVENN';
const GETEVOLVE = 'app/main/GETEVOLVE';
const GETCONTENTIMPROVE = 'app/main/GETCONTENTIMPROVE';
const GETCONTENTEVOLVE = 'app/main/GETCONTENTEVOLVE';
const GETCONTENTDATASET = 'app/main/GETCONTENTDATASET';
const GETCONTENTCROSSTEST = 'app/main/GETCONTENTCROSSTEST';
const GETCONTENTFILTER = 'app/main/GETCONTENTFILTER';
const GETCONTENTABOVEBELOW = 'app/main/GETCONTENTABOVEBELOW';
const GETCONTENTMACHINELEARNING = 'app/main/GETCONTENTMACHINELEARNING';
const GETSINGLEMARKET = 'app/main/GETSINGLEMARKET'
const GETIMPROVEPROFIT = 'app/main/GETIMPROVEPROFIT'
const GETIMPROVEABOVEBELOW = 'app/main/GETIMPROVEABOVEBELOW'
const GETVERIFY = 'app/main/GETVERIFY'

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
    SETMARKET2,
    SETMLMARKET,
    SETMARKETS,
    GETMARKETS,
    SETSTARTDATE,
    SETENDDATE,
    SETCONFIG,
    SETCONFIG2,
    SETCONFIGVALUE,
    SETCONFIGVALUE2,
    SETCONFIGVALUEMAP,
    SETICONFIGVALUEMAP,
    GETCONFIG,
    GETCONTENT,
    GETCONTENT2,
    GETCONTENTGRAPH,
    GETEVOLVERECOMMENDER,
    GETEVOLVENN,
    GETEVOLVE,
    GETCONTENTIMPROVE,
    GETCONTENTEVOLVE,
    GETCONTENTDATASET,
    GETCONTENTCROSSTEST,
    GETCONTENTFILTER,
    GETCONTENTABOVEBELOW,
    GETCONTENTMACHINELEARNING,
    GETSINGLEMARKET,
    GETIMPROVEPROFIT,
    GETIMPROVEABOVEBELOW,
    GETVERIFY,
};

// ------------------------------------
// Actions
// ------------------------------------
export const getAwesomeCode = createAction(GET_MAIN, () => ({}));
export const getAwesomeR3 = createAction(GET_R3, () => ({}));
export const getAwesomeR4 = createAction(GET_R4, () => ({}));
export const updateMain = createAction(UPDATE_MAIN, (result2) => ({ result2 }));
export const updateR3 = createAction(UPDATE_R3, (result3) => ({ result3 }));
export const updateR4 = createAction(UPDATE_R4, (result4) => ({ result4 }));
export const newtabMain3 = createAction(NEWTAB_MAIN3, () => ( new Tab()));
export const newtabMain = createAction(NEWTAB_MAIN, (par) => ( par ) );
//export const increment = createAction(INCREMENT);
export const increment = createAction(INCREMENT, ( num = 1) => ({ num }));
export const increment2 = createAction(INCREMENT2, ( count ) => ({ count }));
export const incrementasync = createAction(INCREMENT_ASYNC, () => ({  }));
export const getCount = createAction(GET_COUNT, () => ({ }));
export const setmarket = createAction(SETMARKET, (market) => ({ market } ) );
export const setimarket = createAction(SETMARKET2, (market) => ({ market } ) );
export const setmlmarket = createAction(SETMLMARKET, (market) => ({ market } ) );
export const getMarkets = createAction(GETMARKETS, () => ( {} ) );
export const setmarkets = createAction(SETMARKETS, (markets) => ( { markets } ) );
export const setstartdate = createAction(SETSTARTDATE, (startdate) => ( { startdate } ) );
export const setenddate = createAction(SETENDDATE, (enddate) => ( { enddate } ) );
export const setconfig = createAction(SETCONFIG, (config) => ( { config } ) );
export const setconfig2 = createAction(SETCONFIG2, (iconfig) => ( { iconfig } ) );
export const setconfigvalue = createAction(SETCONFIGVALUE, ( array ) => ( array ) );
export const seticonfigvalue = createAction(SETCONFIGVALUE2, ( array ) => ( array ) );
export const setconfigvaluemap = createAction(SETCONFIGVALUEMAP, ( array ) => ( array ) );
export const seticonfigvaluemap = createAction(SETICONFIGVALUEMAP, ( array ) => ( array ) );
export const getConfig = createAction(GETCONFIG, () => ( {} ) );
export const getcontent = createAction(GETCONTENT, (config, market, props) => ( { config, market, props } ) );
export const getcontent2 = createAction(GETCONTENT2, (config, market, props) => ( { config, market, props } ) );
export const getcontentgraph = createAction(GETCONTENTGRAPH, (config, value, props) => ( { config, value, props } ) );
export const getevolverecommender = createAction(GETEVOLVERECOMMENDER, () => ( {} ) );
export const getevolvenn = createAction(GETEVOLVENN, () => ( {} ) );
export const getevolve = createAction(GETEVOLVE, (array) => ( { array } ) );
export const getcontentevolve = createAction(GETCONTENTEVOLVE, (config, market, props) => ( { config, market, props } ) );
export const getcontentdataset = createAction(GETCONTENTDATASET, (config, market, props) => ( { config, market, props } ) );
export const getcontentcrosstest = createAction(GETCONTENTCROSSTEST, (config, market, props) => ( { config, market, props } ) );
export const getcontentfilter = createAction(GETCONTENTFILTER, (config, market, props) => ( { config, market, props } ) );
export const getcontentabovebelow = createAction(GETCONTENTABOVEBELOW, (config, market, props) => ( { config, market, props } ) );
export const getcontentimprove = createAction(GETCONTENTIMPROVE, (config, market, props) => ( { config, market, props } ) );
export const getcontentmachinelearning = createAction(GETCONTENTMACHINELEARNING, (config, market, props) => ( { config, market, props } ) );
export const getsinglemarket = createAction(GETSINGLEMARKET, (config, market, props, loop) => ( { config, market, props, loop } ) );
export const getimproveprofit = createAction(GETIMPROVEPROFIT, (config, market, props) => ( { config, market, props } ) );
export const getimproveabovebelow = createAction(GETIMPROVEABOVEBELOW, (config, market, props) => ( { config, market, props } ) );
export const getverify = createAction(GETVERIFY, (config, market, props, loop) => ( { config, market, props, loop } ) );

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
    setimarket,
    setmlmarket,
    setmarkets,
    setstartdate,
    setenddate,
    setconfig,
    setconfig2,
    setconfigvalue,
    seticonfigvalue,
    setconfigvaluemap,
    seticonfigvaluemap,
    getConfig,
    getMarkets,
    getcontent,
    getcontent2,
    getcontentgraph,
    getevolverecommender,
    getevolvenn,
    getevolve,
    getcontentevolve,
    getcontentdataset,
    getcontentcrosstest,
    getcontentfilter,
    getcontentabovebelow,
    getcontentimprove,
    getcontentmachinelearning,
    getsinglemarket,
    getimproveprofit,
    getimproveabovebelow,
    getverify,
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
    [SETMARKET2]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETMLMARKET]: (state, { payload }) =>
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
    [SETCONFIG2]: (state, { payload }) =>
	state.merge({
	    ...payload
	}),
    [SETCONFIGVALUE]: (state, { payload }) =>
	state.merge({
	    config: getConfigAfterSet(state, payload)
    }),
    [SETCONFIGVALUE2]: (state, { payload }) =>
	state.merge({
	    config: getConfigAfterSet(state, payload)
    }),
    [SETCONFIGVALUEMAP]: (state, { payload }) =>
	state.merge({
	    config: getConfigValueMapAfterSet(state, payload)
    }),
  [SETICONFIGVALUEMAP]: (state, { payload }) =>
  state.merge({
    iconfig: getConfigValueMapAfterSet(state, payload)
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

 function gettabs2(state, payload) {
    var tabs = [];
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
    var config = state.get('config');
    return MyMap.myset(config, payload[0], payload[1]);
}

function getConfigValueMapAfterSet(state, payload) {
  var config = state.get(payload[0]);
  const valueMap = MyMap.myget(config, 'configValueMap');
  return MyMap.myset(config, 'configValueMap', MyMap.myset(valueMap, payload[1], payload[2]));
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
      imarket: '',
      markets: [],
      config: '',
      iconfig: '',
  })

export default handleActions(reducers, initialState());
