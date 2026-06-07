import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Action } from '@ngrx/store';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { tap, switchMap, map, catchError } from 'rxjs/operators';
import { of } from 'rxjs';

import { LocalStorageService } from '../local-storage/local-storage.service';
import { OAuth2Service } from './oauth2.service';

import {
  ActionAuthLogin,
  ActionAuthLogout,
  ActionOAuth2Login,
  ActionOAuth2LoginSuccess,
  ActionOAuth2LoginFailure,
  ActionOAuth2Logout,
  ActionOAuth2LogoutSuccess,
  ActionOAuth2RefreshToken,
  ActionOAuth2RefreshTokenSuccess,
  ActionOAuth2RefreshTokenFailure,
  ActionOAuth2LoadUser,
  ActionOAuth2LoadUserSuccess,
  ActionOAuth2LoadUserFailure,
  ActionOAuth2Initialize,
  ActionOAuth2InitializeSuccess,
  ActionOAuth2InitializeFailure,
  AuthActionTypes
} from './auth.actions';

export const AUTH_KEY = 'AUTH';

@Injectable()
export class AuthEffects {
  constructor(
    private actions$: Actions<Action>,
    private localStorageService: LocalStorageService,
    private router: Router,
    private oauth2Service: OAuth2Service
  ) {}

  // Legacy login/logout
  login$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionAuthLogin>(AuthActionTypes.LOGIN),
      tap(() =>
        this.localStorageService.setItem(AUTH_KEY, { isAuthenticated: true })
      )
    );
  }, { dispatch: false });

  logout$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionAuthLogout>(AuthActionTypes.LOGOUT),
      tap(() => {
        this.router.navigate(['']);
        this.localStorageService.setItem(AUTH_KEY, { isAuthenticated: false });
      })
    );
  }, { dispatch: false });

  // OAuth2 login
  oauthLogin$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2Login>(AuthActionTypes.OAUTH2_LOGIN),
      tap(() => {
        this.oauth2Service.login();
      })
    );
  }, { dispatch: false });

  // OAuth2 logout
  oauthLogout$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2Logout>(AuthActionTypes.OAUTH2_LOGOUT),
      tap(() => {
        this.oauth2Service.logout();
      }),
      map(() => new ActionOAuth2LogoutSuccess())
    );
  });

  oauthLogoutSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2LogoutSuccess>(AuthActionTypes.OAUTH2_LOGOUT_SUCCESS),
      tap(() => {
        this.router.navigate(['/']);
        this.localStorageService.removeItem(AUTH_KEY);
      })
    );
  }, { dispatch: false });

  // OAuth2 initialization
  oauthInitialize$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2Initialize>(AuthActionTypes.OAUTH2_INITIALIZE),
      switchMap(() => {
        return this.oauth2Service.loadUserProfile().pipe(
          map(user => {
            if (user) {
              return new ActionOAuth2InitializeSuccess();
            } else {
              return new ActionOAuth2InitializeFailure({
                error: 'No active OAuth2 session found'
              });
            }
          }),
          catchError(error => {
            console.debug('OAuth2 initialization check:', error?.message);
            return of(new ActionOAuth2InitializeSuccess());
          })
        );
      })
    );
  });

  // OAuth2 login success - load user profile
  oauthLoginSuccess$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2LoginSuccess>(AuthActionTypes.OAUTH2_LOGIN_SUCCESS),
      tap(action => {
        this.localStorageService.setItem(AUTH_KEY, {
          isAuthenticated: true,
          provider: 'oauth2',
          user: action.payload.user
        });
      })
    );
  }, { dispatch: false });

  // OAuth2 refresh token
  oauthRefreshToken$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2RefreshToken>(AuthActionTypes.OAUTH2_REFRESH_TOKEN),
      switchMap(() => {
        return this.oauth2Service.refreshToken().pipe(
          switchMap(() => {
            const token = {
              accessToken: this.oauth2Service.getAccessToken() || '',
              idToken: this.oauth2Service.getIdToken() || undefined,
              refreshToken: this.oauth2Service.getRefreshToken() || undefined,
              expiresIn: this.oauth2Service.getAccessTokenExpiration() || undefined
            };
            return of(new ActionOAuth2RefreshTokenSuccess({ token }));
          }),
          catchError(error => {
            console.error('OAuth2 token refresh failed:', error?.message);
            return of(new ActionOAuth2RefreshTokenFailure({ 
              error: error?.message || 'Token refresh failed' 
            }));
          })
        );
      })
    );
  });

  // OAuth2 load user
  oauthLoadUser$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2LoadUser>(AuthActionTypes.OAUTH2_LOAD_USER),
      switchMap(() => {
        return this.oauth2Service.loadUserProfile().pipe(
          map(user => {
            if (user) {
              return new ActionOAuth2LoadUserSuccess({ user });
            } else {
              return new ActionOAuth2LoadUserFailure({ 
                error: 'Failed to load user profile' 
              });
            }
          }),
          catchError(error => {
            console.error('Failed to load OAuth2 user:', error?.message);
            return of(new ActionOAuth2LoadUserFailure({ 
              error: error?.message || 'Failed to load user' 
            }));
          })
        );
      })
    );
  });

  // Handle token refresh failure
  oauthRefreshTokenFailure$ = createEffect(() => {
    return this.actions$.pipe(
      ofType<ActionOAuth2RefreshTokenFailure>(AuthActionTypes.OAUTH2_REFRESH_TOKEN_FAILURE),
      tap(action => {
        console.warn('OAuth2 token refresh failed, user may need to re-authenticate:', action.payload.error);
      })
    );
  }, { dispatch: false });
}