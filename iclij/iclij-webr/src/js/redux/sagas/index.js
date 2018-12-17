import { all } from 'redux-saga/effects'
import { mainSaga } from './mainSaga';


export default function* sagas() {
  yield all([
    ...mainSaga,
  ]);
}
