/**
 * Auth Token Sync Middleware
 * 
 * This middleware watches for auth state changes and automatically
 * updates the API Client with the current auth token.
 */

import { select, takeEvery } from 'redux-saga/effects';
import { setAuthToken, setOnBehalfOf } from '../../common/components/util/Client';
import { selectAccessToken, selectUser } from '../selectors/authSelector';

/**
 * Watch for auth state changes and update Client token
 */
export function* authTokenSyncSaga() {
  // Sync token on every action
  yield takeEvery('*', function* () {
    try {
      const accessToken: string | null = yield select(selectAccessToken);
      const user = yield select(selectUser);
      const onBehalfOf = user?.sub || user?.email || null;

      setAuthToken(accessToken);
      setOnBehalfOf(onBehalfOf);
  });
}
