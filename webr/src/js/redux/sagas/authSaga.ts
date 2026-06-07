/**
 * Auth Saga
 * 
 * Handles OAuth2 flows:
 * - Authorization flow initiation
 * - Token exchange
 * - Token refresh
 * - Logout
 * - Automatic token refresh on expiration
 */

import { call, put, takeEvery, select, fork, delay } from 'redux-saga/effects';
import * as authActions from '../modules/auth';
import oauthConfig, {
  generateAuthorizationUrl,
  exchangeCodeForToken,
  refreshAccessToken,
} from '../../common/config/oauth2.config';
import jwt_decode from 'jwt-decode';

/**
 * Initiate OAuth2 login flow
 */
export function* initializeAuthFlow() {
  try {
    yield put(authActions.initiateLogin());
    const loginUrl: string = yield call(generateAuthorizationUrl, oauthConfig);
    yield put(authActions.setLoginUrl(loginUrl));
    // Redirect to OAuth provider
    window.location.href = loginUrl;
  } catch (error: any) {
    const errorMessage = error?.message || 'Failed to initialize authentication flow';
    yield put(authActions.tokenExchangeFailure(errorMessage));
  }
}

/**
 * Handle OAuth callback with authorization code
 */
export function* handleAuthCallbackSaga(action: any) {
  try {
    const { code, state } = action.payload;

    if (!code) {
      throw new Error('No authorization code received');
    }

    yield put(authActions.handleAuthCallback({ code, state }));

    // Exchange code for tokens
    const tokenResponse: Awaited<ReturnType<typeof exchangeCodeForToken>> = yield call(
      exchangeCodeForToken,
      oauthConfig,
      code,
      state
    );

    yield put(
      authActions.tokenExchangeSuccess({
        access_token: tokenResponse.access_token,
        refresh_token: tokenResponse.refresh_token,
        expires_in: tokenResponse.expires_in,
        token_type: tokenResponse.token_type,
      })
    );

    // Decode JWT to extract user info
    try {
      const decoded: any = jwt_decode(tokenResponse.access_token);
      yield put(
        authActions.setUserInfo({
          sub: decoded.sub,
          email: decoded.email,
          name: decoded.name,
          picture: decoded.picture,
          ...decoded,
        })
      );
    } catch (decodeError) {
      console.warn('Failed to decode token for user info:', decodeError);
    }

    // Clear URL parameters
    window.history.replaceState({}, document.title, window.location.pathname);
  } catch (error: any) {
    const errorMessage = error?.message || 'Failed to handle authentication callback';
    yield put(authActions.tokenExchangeFailure(errorMessage));
  }
}

/**
 * Refresh access token before expiration
 */
export function* tokenRefreshSaga() {
  try {
    const state: any = yield select();
    const { refreshToken, tokenExpiresAt } = state.auth;

    if (!refreshToken) {
      throw new Error('No refresh token available');
    }

    if (!tokenExpiresAt) {
      throw new Error('No token expiration time set');
    }

    // Refresh if token expires in less than 5 minutes
    const timeUntilExpiry = tokenExpiresAt - Date.now();
    if (timeUntilExpiry > 300000) {
      // 5 minutes in milliseconds
      return; // Token still valid
    }

    const refreshResponse: Awaited<ReturnType<typeof refreshAccessToken>> = yield call(
      refreshAccessToken,
      oauthConfig,
      refreshToken
    );

    yield put(
      authActions.tokenRefreshSuccess({
        access_token: refreshResponse.access_token,
        refresh_token: refreshResponse.refresh_token,
        expires_in: refreshResponse.expires_in,
        token_type: refreshResponse.token_type,
      })
    );
  } catch (error: any) {
    const errorMessage = error?.message || 'Failed to refresh token';
    console.error('Token refresh failed:', errorMessage);
    // Token refresh failed, user needs to login again
    yield put(authActions.tokenRefreshFailure());
  }
}

/**
 * Monitor token expiration and refresh automatically
 */
export function* tokenExpirationWatcher() {
  while (true) {
    try {
      const state: any = yield select();
      const { isAuthenticated, tokenExpiresAt } = state.auth;

      if (isAuthenticated && tokenExpiresAt) {
        const timeUntilExpiry = tokenExpiresAt - Date.now();
        if (timeUntilExpiry > 0) {
          // Wait until 5 minutes before expiration
          const waitTime = Math.max(timeUntilExpiry - 300000, 1000);
          yield delay(waitTime);
          yield call(tokenRefreshSaga);
        } else {
          // Token already expired, refresh immediately
          yield call(tokenRefreshSaga);
          yield delay(60000); // Check again in 1 minute
        }
      } else {
        // Not authenticated, check every minute
        yield delay(60000);
      }
    } catch (error) {
      console.error('Error in token expiration watcher:', error);
      yield delay(60000);
    }
  }
}

/**
 * Handle logout
 */
export function* logoutSaga() {
  try {
    yield put(authActions.logout());

    // Optional: Redirect to OAuth provider's logout endpoint
    if (process.env.REACT_APP_OAUTH2_LOGOUT_ENDPOINT) {
      const logoutEndpoint = process.env.REACT_APP_OAUTH2_LOGOUT_ENDPOINT;
      const redirectUri = `${window.location.origin}/`;
      window.location.href = `${logoutEndpoint}?redirect_uri=${encodeURIComponent(redirectUri)}`;
    }
  } catch (error: any) {
    console.error('Logout failed:', error);
    yield put(authActions.logout());
  }
}

/**
 * Root auth saga
 */
export function* authSaga() {
  yield takeEvery('auth/initiateAuthFlow', initializeAuthFlow);
  yield takeEvery('auth/handleAuthCallback', handleAuthCallbackSaga);
  yield takeEvery('auth/logout', logoutSaga);
  yield fork(tokenExpirationWatcher);
}
