import { all, fork } from 'redux-saga/effects'
import { mainSaga } from './mainSaga';
import { authSaga } from './authSaga';
import { authTokenSyncSaga } from './authTokenSyncSaga';


export default function* sagas() {
  yield all([
    ...mainSaga,
    fork(authSaga),
    fork(authTokenSyncSaga),
  ]);
}
