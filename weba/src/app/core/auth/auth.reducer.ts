import { AuthState, AuthProvider } from './auth.models';
import { AuthActions, AuthActionTypes } from './auth.actions';

export const initialState: AuthState = {
  isAuthenticated: false,
  user: null,
  token: null,
  loading: false,
  error: null,
  provider: AuthProvider.NONE,
  lastAuthTime: undefined,
  refreshingToken: false
};

export function authReducer(
  state: AuthState = initialState,
  action: AuthActions
): AuthState {
  switch (action.type) {
    // Legacy auth
    case AuthActionTypes.LOGIN:
      return { ...state, isAuthenticated: true, provider: AuthProvider.LOCAL };

    case AuthActionTypes.LOGOUT:
      return { ...state, isAuthenticated: false, provider: AuthProvider.LOCAL };

    // OAuth2 actions
    case AuthActionTypes.OAUTH2_INITIALIZE:
      return { ...state, loading: true };

    case AuthActionTypes.OAUTH2_INITIALIZE_SUCCESS:
      return { ...state, loading: false };

    case AuthActionTypes.OAUTH2_INITIALIZE_FAILURE:
      return { 
        ...state, 
        loading: false, 
        error: action.payload.error 
      };

    case AuthActionTypes.OAUTH2_LOGIN:
      return { ...state, loading: true, error: null };

    case AuthActionTypes.OAUTH2_LOGIN_SUCCESS:
      return {
        ...state,
        isAuthenticated: true,
        user: action.payload.user,
        token: action.payload.token,
        loading: false,
        error: null,
        provider: AuthProvider.OAUTH2,
      };

    case AuthActionTypes.OAUTH2_LOGIN_FAILURE:
      return {
        ...state,
        isAuthenticated: false,
        loading: false,
        error: action.payload.error
      };

    case AuthActionTypes.OAUTH2_LOGOUT:
      return { ...state, loading: true };

    case AuthActionTypes.OAUTH2_LOGOUT_SUCCESS:
      return {
        ...initialState,
        provider: AuthProvider.OAUTH2
      };

    case AuthActionTypes.OAUTH2_REFRESH_TOKEN:
      return { ...state, loading: true };

    case AuthActionTypes.OAUTH2_REFRESH_TOKEN_SUCCESS:
      return {
        ...state,
        token: action.payload.token,
        loading: false,
        error: null
      };

    case AuthActionTypes.OAUTH2_REFRESH_TOKEN_FAILURE:
      return {
        ...state,
        loading: false,
        error: action.payload.error
      };

    case AuthActionTypes.OAUTH2_LOAD_USER:
      return { ...state, loading: true };

    case AuthActionTypes.OAUTH2_LOAD_USER_SUCCESS:
      return {
        ...state,
        user: action.payload.user,
        loading: false,
        error: null
      };

    case AuthActionTypes.OAUTH2_LOAD_USER_FAILURE:
      return {
        ...state,
        loading: false,
        error: action.payload.error
      };

    default:
      return state;
  }
}
