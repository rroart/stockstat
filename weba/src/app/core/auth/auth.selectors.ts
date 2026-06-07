import { createSelector } from '@ngrx/store';

import { selectAuthState } from '../core.state';
import { AuthState } from './auth.models';

export const selectAuth = createSelector(
  selectAuthState,
  (state: AuthState) => state
);

export const selectIsAuthenticated = createSelector(
  selectAuthState,
  (state: AuthState) => state.isAuthenticated
);

// OAuth2 selectors
export const selectAuthUser = createSelector(
  selectAuthState,
  (state: AuthState) => state.user
);

export const selectAuthToken = createSelector(
  selectAuthState,
  (state: AuthState) => state.token
);

export const selectAuthLoading = createSelector(
  selectAuthState,
  (state: AuthState) => state.loading
);

export const selectAuthError = createSelector(
  selectAuthState,
  (state: AuthState) => state.error
);

export const selectAuthProvider = createSelector(
  selectAuthState,
  (state: AuthState) => state.provider
);

export const selectAccessToken = createSelector(
  selectAuthToken,
  (token) => token?.accessToken || null
);

export const selectIdToken = createSelector(
  selectAuthToken,
  (token) => token?.idToken || null
);

export const selectRefreshToken = createSelector(
  selectAuthToken,
  (token) => token?.refreshToken || null
);

export const selectIsOAuth2Authenticated = createSelector(
  selectIsAuthenticated,
  selectAuthProvider,
  (isAuth, provider) => isAuth && provider === 'oauth2'
);
