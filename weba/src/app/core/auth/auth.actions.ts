import { Action } from '@ngrx/store';
import { OAuthUser, OAuthToken } from './auth.models';

export enum AuthActionTypes {
  // Legacy login/logout
  LOGIN = '[Auth] Login',
  LOGOUT = '[Auth] Logout',
  
  // OAuth2 actions
  OAUTH2_LOGIN = '[Auth] OAuth2 Login',
  OAUTH2_LOGIN_SUCCESS = '[Auth] OAuth2 Login Success',
  OAUTH2_LOGIN_FAILURE = '[Auth] OAuth2 Login Failure',
  OAUTH2_LOGOUT = '[Auth] OAuth2 Logout',
  OAUTH2_LOGOUT_SUCCESS = '[Auth] OAuth2 Logout Success',
  OAUTH2_REFRESH_TOKEN = '[Auth] OAuth2 Refresh Token',
  OAUTH2_REFRESH_TOKEN_SUCCESS = '[Auth] OAuth2 Refresh Token Success',
  OAUTH2_REFRESH_TOKEN_FAILURE = '[Auth] OAuth2 Refresh Token Failure',
  OAUTH2_LOAD_USER = '[Auth] OAuth2 Load User',
  OAUTH2_LOAD_USER_SUCCESS = '[Auth] OAuth2 Load User Success',
  OAUTH2_LOAD_USER_FAILURE = '[Auth] OAuth2 Load User Failure',
  OAUTH2_INITIALIZE = '[Auth] OAuth2 Initialize',
  OAUTH2_INITIALIZE_SUCCESS = '[Auth] OAuth2 Initialize Success',
  OAUTH2_INITIALIZE_FAILURE = '[Auth] OAuth2 Initialize Failure'
}

export class ActionAuthLogin implements Action {
  readonly type = AuthActionTypes.LOGIN;
}

export class ActionAuthLogout implements Action {
  readonly type = AuthActionTypes.LOGOUT;
}

export class ActionOAuth2Login implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOGIN;
}

export class ActionOAuth2LoginSuccess implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOGIN_SUCCESS;
  constructor(public payload: { user: OAuthUser; token: OAuthToken }) {}
}

export class ActionOAuth2LoginFailure implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOGIN_FAILURE;
  constructor(public payload: { error: string }) {}
}

export class ActionOAuth2Logout implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOGOUT;
}

export class ActionOAuth2LogoutSuccess implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOGOUT_SUCCESS;
}

export class ActionOAuth2RefreshToken implements Action {
  readonly type = AuthActionTypes.OAUTH2_REFRESH_TOKEN;
}

export class ActionOAuth2RefreshTokenSuccess implements Action {
  readonly type = AuthActionTypes.OAUTH2_REFRESH_TOKEN_SUCCESS;
  constructor(public payload: { token: OAuthToken }) {}
}

export class ActionOAuth2RefreshTokenFailure implements Action {
  readonly type = AuthActionTypes.OAUTH2_REFRESH_TOKEN_FAILURE;
  constructor(public payload: { error: string }) {}
}

export class ActionOAuth2LoadUser implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOAD_USER;
}

export class ActionOAuth2LoadUserSuccess implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOAD_USER_SUCCESS;
  constructor(public payload: { user: OAuthUser }) {}
}

export class ActionOAuth2LoadUserFailure implements Action {
  readonly type = AuthActionTypes.OAUTH2_LOAD_USER_FAILURE;
  constructor(public payload: { error: string }) {}
}

export class ActionOAuth2Initialize implements Action {
  readonly type = AuthActionTypes.OAUTH2_INITIALIZE;
}

export class ActionOAuth2InitializeSuccess implements Action {
  readonly type = AuthActionTypes.OAUTH2_INITIALIZE_SUCCESS;
}

export class ActionOAuth2InitializeFailure implements Action {
  readonly type = AuthActionTypes.OAUTH2_INITIALIZE_FAILURE;
  constructor(public payload: { error: string }) {}
}

export type AuthActions = 
  | ActionAuthLogin 
  | ActionAuthLogout
  | ActionOAuth2Login
  | ActionOAuth2LoginSuccess
  | ActionOAuth2LoginFailure
  | ActionOAuth2Logout
  | ActionOAuth2LogoutSuccess
  | ActionOAuth2RefreshToken
  | ActionOAuth2RefreshTokenSuccess
  | ActionOAuth2RefreshTokenFailure
  | ActionOAuth2LoadUser
  | ActionOAuth2LoadUserSuccess
  | ActionOAuth2LoadUserFailure
  | ActionOAuth2Initialize
  | ActionOAuth2InitializeSuccess
  | ActionOAuth2InitializeFailure;
