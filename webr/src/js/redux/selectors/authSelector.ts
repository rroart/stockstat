/**
 * Auth Selectors
 * 
 * Memoized selectors for accessing auth state efficiently
 */

import { createSelector } from 'reselect';

// Auth state type (for TypeScript)
interface RootState {
  auth: any;
}

const selectAuthState = (state: RootState) => state.auth;

export const selectIsAuthenticated = createSelector(
  [selectAuthState],
  (auth) => auth.isAuthenticated
);

export const selectIsLoading = createSelector(
  [selectAuthState],
  (auth) => auth.isLoading
);

export const selectAuthError = createSelector(
  [selectAuthState],
  (auth) => auth.error
);

export const selectAccessToken = createSelector(
  [selectAuthState],
  (auth) => auth.accessToken
);

export const selectRefreshToken = createSelector(
  [selectAuthState],
  (auth) => auth.refreshToken
);

export const selectTokenType = createSelector(
  [selectAuthState],
  (auth) => auth.tokenType
);

export const selectUser = createSelector(
  [selectAuthState],
  (auth) => auth.user
);

export const selectTokenExpiresAt = createSelector(
  [selectAuthState],
  (auth) => auth.tokenExpiresAt
);

export const selectIsTokenExpired = createSelector(
  [selectTokenExpiresAt],
  (expiresAt) => {
    if (!expiresAt) return false;
    return Date.now() > expiresAt;
  }
);

export const selectAuthorizationHeader = createSelector(
  [selectAccessToken, selectTokenType],
  (accessToken, tokenType) => {
    if (!accessToken) return null;
    return `${tokenType} ${accessToken}`;
  }
);
