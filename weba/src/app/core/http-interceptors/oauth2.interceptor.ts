import { Injectable, Injector } from '@angular/core';
import {
  HttpEvent,
  HttpInterceptor,
  HttpHandler,
  HttpRequest,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, filter, take } from 'rxjs/operators';
import { Store } from '@ngrx/store';

import { OAuth2Service } from '../auth/oauth2.service';
import { AppState } from '../core.state';
import { ActionOAuth2RefreshToken } from '../auth/auth.actions';

/**
 * OAuth2 HTTP Interceptor
 * Automatically adds Bearer token to outgoing HTTP requests
 * and handles token refresh on 401 responses
 */
@Injectable()
export class OAuth2HttpInterceptor implements HttpInterceptor {
  private store: Store<AppState>;
  private isRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

  constructor(
    private injector: Injector,
    private oauth2Service: OAuth2Service
  ) {
    this.store = injector.get(Store<AppState>);
  }

  intercept(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    // Skip adding auth header for certain endpoints
    if (this.shouldSkipInterceptor(request)) {
      return next.handle(request);
    }

    // Add authorization and on-behalf-of headers if token is available
    const token = this.oauth2Service.getAccessToken();
    if (token) {
      request = this.addAuthorizationHeader(request, token);
      request = this.addOnBehalfOfHeader(request, token);
    }

    return next.handle(request).pipe(
      catchError(error => {
        if (error instanceof HttpErrorResponse && error.status === 401) {
          return this.handle401Error(request, next);
        }
        return throwError(() => error);
      })
    );
  }

  /**
   * Add authorization header to request
   */
  private addAuthorizationHeader(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'Authorization': `Bearer ${token}`
      }
    });
  }

  private addOnBehalfOfHeader(request: HttpRequest<any>, token: string): HttpRequest<any> {
    return request.clone({
      setHeaders: {
        'X-On-Behalf-Of': `Bearer ${token}`
      }
    });
  }

  /**
   * Handle 401 Unauthorized errors by attempting to refresh the token
   */
  private handle401Error(
    request: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    if (!this.isRefreshing) {
      this.isRefreshing = true;
      this.refreshTokenSubject.next(null);

      return this.oauth2Service.refreshToken().pipe(
        switchMap((success: boolean) => {
          if (success) {
            const newToken = this.oauth2Service.getAccessToken();
            if (newToken) {
              this.refreshTokenSubject.next(newToken);
              const authRequest = this.addAuthorizationHeader(request, newToken);
              return next.handle(authRequest);
            }
          }
          // If refresh failed, throw error to trigger logout
          this.isRefreshing = false;
          return throwError(() => new Error('Token refresh failed'));
        }),
        catchError(error => {
          this.isRefreshing = false;
          console.error('Token refresh failed:', error);
          // Could dispatch logout action here if needed
          return throwError(() => error);
        })
      );
    } else {
      // Wait for token refresh to complete, then retry with new token
      return this.refreshTokenSubject.pipe(
        filter(token => token !== null),
        take(1),
        switchMap(token => {
          if (token) {
            const authRequest = this.addAuthorizationHeader(request, token);
            return next.handle(authRequest);
          }
          return throwError(() => new Error('Token refresh timed out'));
        })
      );
    }
  }

  /**
   * Determine if the interceptor should be skipped for this request
   */
  private shouldSkipInterceptor(request: HttpRequest<any>): boolean {
    const url = request.url.toLowerCase();

    // Skip for token endpoint requests to avoid adding Bearer token
    if (url.includes('token') || url.includes('/oauth')) {
      return true;
    }

    // Skip for authentication endpoints
    if (url.includes('/auth/') || url.includes('/login') || url.includes('/logout')) {
      return true;
    }

    // Don't skip other API requests - they should have the Bearer token
    return false;
  }
}
