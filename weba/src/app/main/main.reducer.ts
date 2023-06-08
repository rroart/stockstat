import { MainState } from './main.model';
import { MainActions, MainActionTypes } from './main.actions';

export const initialState: MainState = {
  count: 0,
  tabs: [],
  startdate: null,
  enddate: null,
  market: null,
  market2: null,
  markets: [],
  tasks: [],
  config: null,
  config2: null,
};

export function mainReducer(
  state: MainState = initialState,
  action: MainActions
): MainState {
  switch (action.type) {
    case MainActionTypes.NEWTAB:
      return { ...state, tabs: gettabs4(state, action.payload) };
    case MainActionTypes.ERROR:
      console.log(action.payload);
      return state;
    case MainActionTypes.SETCONFIG:
    case MainActionTypes.SETCONFIG2:
      console.log('setconfig');
    case MainActionTypes.SETMARKET:
    case MainActionTypes.SETMARKET2:
    case MainActionTypes.SETMARKETS:
    case MainActionTypes.SETTASKS:
    case MainActionTypes.SETSTARTDATE:
    case MainActionTypes.SETENDDATE:
      console.log(action);
      console.log("here");
      return { ...state, ...action.payload };
      //return { ...state };
    case MainActionTypes.GETMARKETS:
      return { ...state };
    case MainActionTypes.GETCONFIG:
    case MainActionTypes.GETCONFIG2:
      return { ...state };
    case MainActionTypes.INCREMENT:
      console.log(state);
      //console.log(...state);
      var pl = action.payload;
      console.log(pl);
      console.log(action);
      //return { ...state, state.get('count') + action.payload.incCount };
      console.log( { ...state, ...action.payload });
      console.log(state.count + 1);
      console.log(state);
      //state['count'] = state.count + 1;
      //return state;
      //const pl2 = { count: state.count + 1 };
      //return state.set('count', state.count + 1 );
      //return { count: state.count + 1 };
      //return { ...state, { count: state.count + 1 } };
      return { ...state, count: state.count + 1 };
      //return { ...state, ...action.payload };
      //return { ...state, { count: state.count + 1 } };
    case MainActionTypes.SETCONFIGVALUE:
      return { ...state, config: getConfigAfterSet(state, action.payload) };
    case MainActionTypes.SETCONFIGVALUE2:
      //const newpayload = { config: getConfigAfterSet(state, payload) };
      return { ...state, config: getConfigAfterSet2(state, action.payload) };
    case MainActionTypes.SETCONFIGVALUEMAP:
      return { ...state, config: getConfigValueMapAfterSet(state, action.payload) };
    case MainActionTypes.SETCONFIGVALUEMAP2:
      //const newpayload2 = { config: getConfigValueMapAfterSet(state, payload) };
      //return { ...state, config: { action.payload[0]: action.payload[1] } };
      return { ...state, config2: getConfigValueMapAfterSet2(state, action.payload) };

    default:
      return state;
  }
}

function getConfigAfterSet(state, payload) {
    var config = state.config;
    config = Object.assign({}, config);
    console.log(state);
    console.log(payload);
    config[payload[0]] = payload[1];
    return config;
}

function getConfigAfterSet2(state, payload) {
    var config = state.config2;
    config = Object.assign({}, config);
    console.log(state);
    console.log(payload);
    config[payload[0]] = payload[1];
    return config;
}

function getConfigValueMapAfterSet(state, payload) {
    var config = state.config;
    var valueMap = config.configValueMap;
    valueMap = Object.assign({}, valueMap);
    //console.log(valueMap);
    //console.log(typeof valueMap);
    valueMap[payload[0]] = payload[1];
    console.log("here");
    //valueMap = Object.defineProperty(valueMap, payload[0], { value: payload[1] });
    console.log("here");
    config = Object.assign({}, config);
    config['configValueMap'] = valueMap;
    console.log(config);
    return config;
}

function getConfigValueMapAfterSet2(state, payload) {
    var config = state.config2;
    var valueMap = config.configValueMap;
    valueMap = Object.assign({}, valueMap);
    //console.log(valueMap);
    //console.log(typeof valueMap);
    valueMap[payload[0]] = payload[1];
    console.log("here");
    //valueMap = Object.defineProperty(valueMap, payload[0], { value: payload[1] });
    console.log("here");
    config = Object.assign({}, config);
    config['configValueMap'] = valueMap;
    console.log(config);
    return config;
}

function gettabs4(state, payload) {
    console.log("state0");
    console.log(state);
    console.log(payload);
    var arr = state.tabs;
    console.log(arr);
    arr = Object.assign([], arr);
    console.log(arr);
    var arrayLength = arr.length;
    var newpay = payload + arrayLength;
    arr.push(payload);
    console.log("state1");
    console.log(state);
    console.log(arr);
    return arr;
}
