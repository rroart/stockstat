/**
 * Auth Token Sync Middleware
 * 
 * This middleware watches for auth state changes and automatically
 * updates the API Client with the current auth token.
 */

import { select, takeEvery } from 'redux-saga/effects';
import { setAuthToken } from '../../common/components/util/Client';
import { selectAccessToken } from '../selectors/authSelector';

/**
 * Watch for auth state changes and update Client token
 */
export function* authTokenSyncSaga() {
  // Sync token on every action
  yield takeEvery('*', function* () {
    try {
      const accessToken: string | null = yield select(selectAccessToken);
      setAuthToken(accessToken);
    } catch (error) {
      console.error('Error syncing auth token:', error);
    }
  });
}
