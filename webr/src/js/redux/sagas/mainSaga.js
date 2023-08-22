import { put, fork, takeLatest, takeEvery, call, delay } from 'redux-saga/effects';
import { constants as mainConstants, actions as mainActions } from '../modules/main';
import { Tabs, Tab } from 'react-bootstrap';

import { ConfigMaps, ConfigData, GuiSize } from '../../common/types/main'

import { Client, ConvertToSelect } from '../../common/components/util'
import { ServiceParam, ServiceResult, NeuralNetCommand, IclijServiceParam, IclijServiceResult } from '../../common/types/main'
import { MyTable } from '../../common/components/Table'

export function* fetchMainData() {
  // pretend there is an api call
  const result = {
    title: 'Stockstat',
    description: __CONFIG__.description,
    source: 'This message is coming from Redux',
  };
  //const result3 = 3;
  console.log("blblfirst");
  yield put(mainActions.updateMain(result));
}

export function* fetchConfig() {
    var serviceparam = new IclijServiceParam();
    //serviceparam.market = '0';
    console.log("hereconfig");
    let config = yield call(Client.fetchApi.search, "/getconfig", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.configData));
}

export function* fetchConfig2() {
    var serviceparam = new IclijServiceParam();
    //serviceparam.market = '0';
    console.log("hereconfig");
    let config = yield call(Client.fetchApi.search3, "/getconfig", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig2(config2.configData));
}

function getConfigData(config, market, date) {
    const myconfig = new ConfigData();
    myconfig.configTreeMap = config.get('configTreeMap');
    myconfig.configValueMap = config.get('configValueMap');
    const configmaps = new ConfigMaps();
    myconfig.configMaps = configmaps;
    configmaps.text = config.get('text');
    configmaps.deflt = config.get('deflt');
    configmaps.type = config.get('map');
    myconfig.date = date;
    myconfig.market = market;
    return myconfig;
}

export function* fetchContent(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    var neuralnetcommand = new NeuralNetCommand();
    neuralnetcommand.mllearn = false;
    neuralnetcommand.mlclassify = true;
    neuralnetcommand.mldynamic = false;
    serviceparam.neuralnetcommand = neuralnetcommand;
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

export function* fetchContent2NO(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    var neuralnetcommand = new NeuralNetCommand();
    neuralnetcommand.mllearn = false;
    neuralnetcommand.mlclassify = true;
    neuralnetcommand.mldynamic = false;
    serviceparam.neuralnetcommand = neuralnetcommand;
    let result = yield call(Client.fetchApi.search2, "/getcontent", serviceparam);
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
    var serviceparam = new IclijServiceParam();
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
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
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
    var serviceparam = new IclijServiceParam();
    //serviceparam.market = '0';
    console.log("here");
    let config = yield call(Client.fetchApi.search, "/getevolverecommender", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.configData));
}

export function* fetchEvolveNN() {
    var serviceparam = new IclijServiceParam();
    //serviceparam.market = '0';
    console.log("hereconfig");
    let config = yield call(Client.fetchApi.search, "/getevolvenn", serviceparam);
    console.log("hereconfig2");
    console.log(config);
    const config2 = config;
    console.log(config2);
    yield put(mainActions.setconfig(config2.configData));
}

export function* fetchEvolve(action) {
    console.log(action);
    const array = action.payload.array;
    const props = action.payload.props;
    const webpath = array[0];
    const set = array[1];
    const config = array[2];
    const id = array[3];
    const serviceparam = new IclijServiceParam();
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    console.log(date);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
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

//export function* fetchIclijContent(action) {
export function* fetchContent2(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    console.log(date);
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontent", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentEvolve(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search, "/getcontentevolve", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentDataset(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentdataset", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentCrosstest(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentcrosstest", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentFilter(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentfilter", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentAboveBelow(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentabovebelow", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentImprove(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentimprove", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchContentMachineLearning(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/getcontentmachinelearning", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    console.log(list);
    const tab = {}; //IclijMyTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchSingleMarket(action) {
    var serviceparam = new IclijServiceParam();
    const config = action.payload.config;
    serviceparam.market = config.get('market');
    console.log(action);
    const props = action.payload.props;
    const loop = action.payload.loop;
    var date = config.get('enddate');
    let loops = 1
    if (loop) {
	date = config.get('startdate');
    }
    console.log(loop);
    console.log(date);
    const configData = getConfigData(config, serviceparam.market, date);
    serviceparam.configData = configData;
    if (loop) {
	loops = config.configValueMap.get('singlemarket.loops');
    }
    var i = 0;
    for (i = 0; i < loops; i++) {
	console.log(serviceparam.market);
	//serviceparam.offset = i * config.get('singlemarket').get('loopinterval');
	if (loop) {
	    serviceparam.offset = i * serviceparam.configData.configValueMap.get('singlemarket.loopinterval');
	}
	console.log("herecontent");
	console.log(serviceparam.market);
	let result = yield call(Client.fetchApi.search2, "/findprofit", serviceparam);
	console.log("herecontent2");
	console.log(result);
	console.log(action);
	const config2 = result;
	console.log(config2);
	const list = result.lists;
	const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
	yield put(mainActions.newtabMain(tab));
    }
}

export function* fetchImproveProfit(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/improveprofit", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchImproveAboveBelow(action) {
    var serviceparam = new IclijServiceParam();
    console.log(action);
    const config = action.payload.config;
    const props = action.payload.props;
    const date = config.get('enddate');
    serviceparam.market = config.get('market');
    console.log(serviceparam.market);
    serviceparam.configData = getConfigData(config, serviceparam.market, date);
    console.log("herecontent");
    console.log(serviceparam.market);
    let result = yield call(Client.fetchApi.search2, "/improveabovebelow", serviceparam);
    console.log("herecontent2");
    console.log(result);
    console.log(action);
    const config2 = result;
    console.log(config2);
    const list = result.lists;
    const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
    yield put(mainActions.newtabMain(tab));
}

export function* fetchGetVerify(action) {
    var serviceparam = new IclijServiceParam();
    const config = action.payload.config;
    serviceparam.market = config.get('market');
    console.log(action);
    const props = action.payload.props;
    const loop = action.payload.loop;
    var date = config.get('enddate');
    let loops = 1
    if (loop) {
	date = config.get('startdate');
    }
    console.log(loop);
    console.log(date);
    const configData = getConfigData(config, serviceparam.market, date);
    serviceparam.configData = configData;
    if (loop) {
	loops = config.configDataValueMap.get('verification.loops');
    }
    var i = 0;
    for (i = 0; i < loops; i++) {
	console.log(serviceparam.market);
	console.log(config);
	console.log(Object.keys(config));
	console.log(config.get('verification'));
	console.log(config['verification']);
	console.log(serviceparam.configData);
	//serviceparam.offset = i * config.get('verification').get('loopinterval');
	console.log(serviceparam.configData.configValueMap.get('verification.loopinterval'))
	if (loop) {
	    serviceparam.offset = i * serviceparam.configData.configValueMap.get('verification.loopinterval');
	}
	console.log("herecontent");
	console.log(serviceparam.market);
	let result = yield call(Client.fetchApi.search2, "/getverify", serviceparam);
	console.log("herecontent2");
	console.log(result);
	console.log(action);
	const config2 = result;
	console.log(config2);
	const list = result.lists;
	const tab = MyIclijTable.getTab(result.lists, Date.now(), props);
	yield put(mainActions.newtabMain(tab));
    }
}

export function* fetchMarkets() {
    console.log("heremarkets");
    const serviceparam = new IclijServiceParam();
    //serviceparam.market = '0';
    console.log("heremarkets1");
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

function* watchGetConfig2() {
    console.log("watchgetconfig2");
  yield takeLatest(mainConstants.GETCONFIG, fetchConfig2);
}

function* watchGetContent() {
    console.log("watchgetcontent");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENT, fetchContent);
}

function* watchGetContent2() {
    console.log("watchgetcontent");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENT2, fetchContent2);
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

function* watchGetContentImprove() {
    console.log("watchgetimprove");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTIMPROVE, fetchContentImprove);
}

function* watchGetContentEvolve() {
    console.log("watchgetcontentevolve");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTEVOLVE, fetchContentEvolve);
}

function* watchGetContentDataset() {
    console.log("watchgetcontentdataset");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTDATASET, fetchContentDataset);
}

function* watchGetContentCrosstest() {
    console.log("watchgetcontentcrosstest");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTCROSSTEST, fetchContentCrosstest);
}

function* watchGetContentFilter() {
    console.log("watchgetcontentfilter");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTFILTER, fetchContentFilter);
}

function* watchGetContentAboveBelow() {
    console.log("watchgetcontentabovebelow");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTABOVEBELOW, fetchContentAboveBelow);
}

function* watchGetContentMachineLearning() {
    console.log("watchgetcontentmachinelearning");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETCONTENTMACHINELEARNING, fetchContentMachineLearning);
}

function* watchGetSingleMarket() {
    console.log("watchgetsinglemarket");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETSINGLEMARKET, fetchSingleMarket);
}

function* watchGetImproveProfit() {
    console.log("watchgetimproveprofit");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETIMPROVEPROFIT, fetchImproveProfit);
}

function* watchGetImproveAboveBelow() {
    console.log("watchgetimproveabovebelow");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETIMPROVEABOVEBELOW, fetchImproveAboveBelow);
}

function* watchGetVerify() {
    console.log("watchgetcontent");
    //console.log(action);
    //const config = null;
    //console.log(config);
    yield takeEvery(mainConstants.GETVERIFY, fetchGetVerify);
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
    fork(watchGetConfig2),
    fork(watchGetContent),
    fork(watchGetContent2),
    fork(watchGetContentGraph),
    fork(watchGetEvolveRecommender),
    fork(watchGetEvolveNN),
    fork(watchGetEvolve),
    fork(watchGetContentImprove),
    fork(watchGetContentEvolve),
    fork(watchGetContentDataset),
    fork(watchGetContentCrosstest),
    fork(watchGetContentFilter),
    fork(watchGetContentAboveBelow),
    fork(watchGetContentMachineLearning),
    fork(watchGetSingleMarket),
    fork(watchGetImproveProfit),
    fork(watchGetImproveAboveBelow),
    fork(watchGetVerify),
];
