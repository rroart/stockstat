import { put, fork, takeLatest, takeEvery } from 'redux-saga/effects';
import { delay } from 'redux-saga';
import { constants as mainConstants, actions as mainActions } from '../modules/main';
import { Tabs, Tab } from 'react-bootstrap';

import type { mainType } from '../../common/types/main'

export function* fetchMainData() {
  // pretend there is an api call
  const result: mainType = {
    title: 'Myweb',
    description: __CONFIG__.description,
    source: 'This message is coming from Redux',
  };
  //const result3 = 3;
  console.log("blblfirst");
  yield put(mainActions.updateMain(result));
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
];
