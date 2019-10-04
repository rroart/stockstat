import { put, fork, takeLatest, takeEvery, call } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { constants as mainConstants, actions as mainActions } from '../modules/main';
import { Tabs, Tab } from 'react-bootstrap';

import type { mainType, MyConfig, GuiSize } from '../../common/types/main'

import { Client, ConvertToSelect } from '../../common/components/util'
import { ServiceParam, ServiceResult } from '../../common/types/main'
import { MyTable } from '../../common/components/Table'

export function* fetchMainData() {
  // pretend there is an api call
  const result: mainType = {
    title: 'Stockstat',
    description: __CONFIG__.description,
    source: 'This message is coming from Redux',
  };
  //const result3 = 3;
  console.log("blblfirst");
  yield put(mainActions.updateMain(result));
}

export function* fetchConfig() {
    var serviceparam = new ServiceParam();
    //serviceparam.market = '0';
    console.log("hereconfig");
    let config = yield call(Client.fetchApi.search, "/getconfig", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.config));
}

function getMyConfig(config, market, date) {
    const myconfig = new MyConfig();
    myconfig.configTreeMap = config.get('configTreeMap');
    myconfig.configValueMap = config.get('configValueMap');
    myconfig.text = config.get('text');
    myconfig.deflt = config.get('deflt');
    myconfig.type = config.get('type');
    myconfig.date = date;
    myconfig.market = market;
    return myconfig;
}

export function* fetchContent(action) {
    var serviceparam = new ServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.config = getMyConfig(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search, "/getcontent", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.list;
    const tab = MyTable.getTab(result.list, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentGraph(action) {
    var serviceparam = new ServiceParam();
    console.log(action);
    const config = action.payload.config;
    const id = action.payload.value;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    const ids = new Set([serviceparam.market + "," + id]);
    serviceparam.ids = ids;
    console.log(serviceparam.market);
    var guisize = new GuiSize();
    guisize.x=600;
    guisize.y=400;
    serviceparam.guiSize = guisize;
    serviceparam.config = getMyConfig(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    console.log(serviceparam);
    let result = yield call(Client.fetchApi.search, "/getcontentgraph2", serviceparam);
    console.log("herecontent2");
    console.log(result);
    const config2 = result;
    console.log(config2);
    const list = result.list;
    const tab = MyTable.getTab(result.list, Date.now(), props);
    console.log(tab);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchEvolveRecommender() {
    var serviceparam = new ServiceParam();
    //serviceparam.market = '0';
    console.log("here");
    let config = yield call(Client.fetchApi.search, "/getevolverecommender", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.config));
}

export function* fetchEvolveNN() {
    var serviceparam = new ServiceParam();
    //serviceparam.market = '0';
    console.log("hereconfig");
    let config = yield call(Client.fetchApi.search, "/getevolvenn", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.config));
}

export function* fetchEvolve(action) {
    console.log(action);
    const array = action.payload.array;
    const props = action.payload.props;
    const webpath = array[0];
    const set = array[1];
    const config = array[2];
    const id = array[3];
    const serviceparam = new ServiceParam();
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    console.log(date);
    serviceparam.config = getMyConfig(config, serviceparam.market, date);
    if (id != null) {
	const ids = new Set([id]);
	serviceparam.ids = ids;
    }
    //serviceparam.market = '0';
    console.log("hereevolve");
    console.log(webpath);
    let result = yield call(Client.fetchApi.search, "/" + webpath, serviceparam);
    console.log("hereevolve2");
    console.log(result);
    if (set) {
	const update = result.get("maps").get("update");
	for (const [key, value] of Object.entries(update)) {
	    mainActions.setconfigvaluemap([ key, value ]);
	}
    }
    const list = result.list;
    const tab = MyTable.getTab(result.list, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchMarkets() {
    var serviceparam = new ServiceParam();
    //serviceparam.market = '0';
    console.log("heremarkets");
    let markets = yield call(Client.fetchApi.search, "/getmarkets", serviceparam);
    console.log("heremarkets2");
    //const markets = Client.searchsynch("/getmarkets", serviceparam, () => {});
    console.log(markets);
	yield put(mainActions.setmarkets(markets.markets));
    /*
    Client.search("/getmarkets", serviceparam, (markets) => {
    console.log("here");
      console.log(markets);
      console.log(this.props);
      //this.props.setmarkets(markets.markets);
	yield put(mainActions.setmarkets(markets.markets));
    });
    */
}

export function* fetchR3() {
  // pretend there is an api call
    const result = 'hei';
  //const result = 3;
  console.log("blbl0");
  yield put(mainActions.updateR3(result));
}

export function* fetchR4() {
  // pretend there is an api call
    const result = 'ieh';
  //const result = 3;
  console.log("blbl1");
  yield put(mainActions.updateR4(result));
}

export function* fetchCount() {
  // pretend there is an api call
    const result = 0;
    //const result = 3;
    //console.log(bl);
  console.log("blbl2");
  //yield put(mainActions.getCount(result));
}

export function* getNewTab() {
    console.log("bla")
    const result = new Tab();
  yield put(mainActions.newtabMain(result));
}

// Our Worker Saga: will perform the ansync increment task
export function* incrementAsync() {
    yield delay(5000); // sleeps for 1 second, yield will suspend the Saga until the Promise completes
    console.log('delay');
    yield put(mainActions.increment());
}

// Our watcher Saga: spawn a new incrementAsync task on each INCREMENT_ASYNC
function* watchIncrementAsync() {
  yield takeEvery(mainConstants.INCREMENT_ASYNC, incrementAsync);
}

function* watchCount() {
  yield takeEvery(mainConstants.GET_COUNT, fetchCount);
}

function* watchGetMain() {
  yield takeLatest(mainConstants.GET_MAIN, fetchMainData);
}

function* watchGetMarkets() {
    console.log("watchgetmarkets");
  yield takeLatest(mainConstants.GETMARKETS, fetchMarkets);
}

function* watchGetConfig() {
    console.log("watchgetconfig");
  yield takeLatest(mainConstants.GETCONFIG, fetchConfig);
}

function* watchGetContent() {
    console.log("watchgetcontent");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENT, fetchContent);
}

function* watchGetContentGraph() {
    console.log("watchgetcontent");
    yield takeEvery(mainConstants.GETCONTENTGRAPH, fetchContentGraph);
}

function* watchGetEvolveRecommender() {
    console.log("watchgetrecommender");
  yield takeEvery(mainConstants.GETEVOLVERECOMMENDER, fetchEvolveRecommender);
}

function* watchGetEvolveNN() {
    console.log("watchgetevolvenn");
  yield takeEvery(mainConstants.GETEVOLVENN, fetchEvolveNN);
}

function* watchGetEvolve() {
    console.log("watchgetevolve");
  yield takeEvery(mainConstants.GETEVOLVE, fetchEvolve);
}

function* watchGetR3() {
  yield takeLatest(mainConstants.GET_R3, fetchR3);
}

function* watchGetR4() {
  yield takeLatest(mainConstants.GET_R4, fetchR4);
}

function* watchNewTabMain() {
  console.log("bla")
  //yield takeLatest(mainConstants.NEWTAB_MAIN, getNewTab);
}

export const mainSaga = [
  fork(watchGetMain),
  fork(watchNewTabMain),
  fork(watchGetR3),
  fork(watchGetR4),
  fork(watchIncrementAsync),
    fork(watchCount),
    fork(watchGetMarkets),
    fork(watchGetConfig),
    fork(watchGetContent),
    fork(watchGetContentGraph),
    fork(watchGetEvolveRecommender),
    fork(watchGetEvolveNN),
    fork(watchGetEvolve),
];
