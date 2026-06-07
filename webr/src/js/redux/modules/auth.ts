/**
 * Auth Reducer Module
 * 
 * Manages OAuth2 authentication state including:
 * - Access token and refresh token storage
 * - User information
 * - Authentication status
 * - Token expiration tracking
 */

import { createSlice, PayloadAction } from '@reduxjs/toolkit';

export interface AuthUser {
  sub?: string;
  email?: string;
  name?: string;
  picture?: string;
  [key: string]: any;
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  accessToken: string | null;
  refreshToken: string | null;
  tokenType: string;
  expiresIn: number | null;
  tokenExpiresAt: number | null;
  user: AuthUser | null;
  loginUrl: string | null;
  callbackCode: string | null;
  callbackState: string | null;
}

const initialState: AuthState = {
  isAuthenticated: false,
  isLoading: false,
  error: null,
  accessToken: localStorage.getItem('oauth2_access_token'),
  refreshToken: localStorage.getItem('oauth2_refresh_token'),
  tokenType: 'Bearer',
  expiresIn: null,
  tokenExpiresAt: localStorage.getItem('oauth2_expires_at')
    ? parseInt(localStorage.getItem('oauth2_expires_at')!, 10)
    : null,
  user: localStorage.getItem('oauth2_user')
    ? JSON.parse(localStorage.getItem('oauth2_user')!)
    : null,
  loginUrl: null,
  callbackCode: null,
  callbackState: null,
};

// Check if token is still valid on initialization
if (
  initialState.accessToken &&
  initialState.tokenExpiresAt &&
  initialState.tokenExpiresAt > Date.now()
) {
  initialState.isAuthenticated = true;
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    // Initialize login flow
    initiateLogin: (state) => {
      state.isLoading = true;
      state.error = null;
    },

    // Set login URL
    setLoginUrl: (state, action: PayloadAction<string>) => {
      state.loginUrl = action.payload;
    },

    // Handle OAuth callback
    handleAuthCallback: (state, action: PayloadAction<{ code: string; state: string }>) => {
      state.callbackCode = action.payload.code;
      state.callbackState = action.payload.state;
      state.isLoading = true;
      state.error = null;
    },

    // Token exchange success
    tokenExchangeSuccess: (
      state,
      action: PayloadAction<{
        access_token: string;
        refresh_token?: string;
        expires_in?: number;
        token_type: string;
      }>
    ) => {
      const { access_token, refresh_token, expires_in, token_type } = action.payload;

      state.accessToken = access_token;
      state.refreshToken = refresh_token || state.refreshToken;
      state.tokenType = token_type || 'Bearer';
      state.expiresIn = expires_in || null;

      // Calculate expiration time (5 minutes before actual expiry for safety)
      if (expires_in) {
        state.tokenExpiresAt = Date.now() + (expires_in - 300) * 1000;
      }

      state.isAuthenticated = true;
      state.isLoading = false;

      // Persist to localStorage
      localStorage.setItem('oauth2_access_token', access_token);
      if (refresh_token) {
        localStorage.setItem('oauth2_refresh_token', refresh_token);
      }
      if (state.tokenExpiresAt) {
        localStorage.setItem('oauth2_expires_at', state.tokenExpiresAt.toString());
      }
    },

    // Set user info (from ID token or user info endpoint)
    setUserInfo: (state, action: PayloadAction<AuthUser>) => {
      state.user = action.payload;
      localStorage.setItem('oauth2_user', JSON.stringify(action.payload));
    },

    // Token exchange failure
    tokenExchangeFailure: (state, action: PayloadAction<string>) => {
      state.isLoading = false;
      state.error = action.payload;
      state.isAuthenticated = false;
    },

    // Token refresh success
    tokenRefreshSuccess: (
      state,
      action: PayloadAction<{
        access_token: string;
        refresh_token?: string;
        expires_in?: number;
        token_type: string;
      }>
    ) => {
      const { access_token, refresh_token, expires_in, token_type } = action.payload;

      state.accessToken = access_token;
      if (refresh_token) {
        state.refreshToken = refresh_token;
      }
      state.tokenType = token_type || 'Bearer';

      if (expires_in) {
        state.expiresAt = Date.now() + (expires_in - 300) * 1000;
      }

      localStorage.setItem('oauth2_access_token', access_token);
      if (refresh_token) {
        localStorage.setItem('oauth2_refresh_token', refresh_token);
      }
    },

    // Token refresh failure
    tokenRefreshFailure: (state) => {
      state.error = 'Token refresh failed. Please login again.';
      state.isAuthenticated = false;
      state.accessToken = null;
      state.refreshToken = null;
      state.user = null;
      
      // Clear localStorage
      localStorage.removeItem('oauth2_access_token');
      localStorage.removeItem('oauth2_refresh_token');
      localStorage.removeItem('oauth2_expires_at');
      localStorage.removeItem('oauth2_user');
    },

    // Logout
    logout: (state) => {
      state.isAuthenticated = false;
      state.accessToken = null;
      state.refreshToken = null;
      state.tokenExpiresAt = null;
      state.user = null;
      state.error = null;
      state.isLoading = false;
      state.callbackCode = null;
      state.callbackState = null;

      // Clear localStorage
      localStorage.removeItem('oauth2_access_token');
      localStorage.removeItem('oauth2_refresh_token');
      localStorage.removeItem('oauth2_expires_at');
      localStorage.removeItem('oauth2_user');
      sessionStorage.removeItem('oauth2_code_verifier');
    },

    // Clear error
    clearError: (state) => {
      state.error = null;
    },
  },
});

export const {
  initiateLogin,
  setLoginUrl,
  handleAuthCallback,
  tokenExchangeSuccess,
  setUserInfo,
  tokenExchangeFailure,
  tokenRefreshSuccess,
  tokenRefreshFailure,
  logout,
  clearError,
} = authSlice.actions;

export default authSlice.reducer;
