import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { OAuthService, OAuthEvent, OAuthErrorEvent, AuthConfig } from 'angular-oauth2-oidc';
import { BehaviorSubject, Observable, from, of, firstValueFrom } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { OAuthUser } from './auth.models';

@Injectable({
  providedIn: 'root'
})
export class OAuth2Service {
  private userSubject = new BehaviorSubject<OAuthUser | null>(null);
  public user$ = this.userSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(false);
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  private tokenExpirationSubject = new BehaviorSubject<number | null>(null);
  public tokenExpiration$ = this.tokenExpirationSubject.asObservable();

  constructor(private oauthService: OAuthService, private http: HttpClient) {
    this.setupEventListeners();
  }

  /**
   * Initialize OAuth2 with the provided configuration
   */
  public initializeOAuth2(config: AuthConfig): Promise<boolean> {
    this.oauthService.configure(config);
    this.oauthService.setupAutomaticSilentRefresh();

    if (config.issuer) {
      return this.oauthService
        .loadDiscoveryDocumentAndTryLogin()
        .then(async () => {
          this.updateAuthenticationStatus();
          if (this.oauthService.hasValidAccessToken()) {
            await firstValueFrom(this.loadUserProfile());
          }
          return true;
        })
        .catch(error => {
          console.error('Failed to initialize OAuth2 discovery/login:', error);
          this.updateAuthenticationStatus();
          return false;
        });
    }

    return this.oauthService
      .tryLoginCodeFlow()
      .then(async () => {
        this.updateAuthenticationStatus();
        if (this.oauthService.hasValidAccessToken()) {
          await firstValueFrom(this.loadUserProfile());
        }
        return true;
      })
      .catch(error => {
        console.error('Failed to initialize OAuth2 code flow login:', error);
        this.updateAuthenticationStatus();
        return false;
      });
  }

  /**
   * Setup OAuth2 event listeners
   */
  private setupEventListeners(): void {
    this.oauthService.events.subscribe((event: OAuthEvent | OAuthErrorEvent) => {
      if (event instanceof OAuthErrorEvent) {
        console.error('OAuth2 Error:', event);
      } else {
        console.debug('OAuth2 Event:', event.type);
      }

      if (event.type === 'token_received' || event.type === 'token_refreshed') {
        this.updateAuthenticationStatus();
        this.loadUserProfile().subscribe();
      }

      if (event.type === 'logout') {
        this.updateAuthenticationStatus();
        this.userSubject.next(null);
      }
    });
  }

  /**
   * Initiate OAuth2 login flow
   */
  public login(): void {
    this.oauthService.initCodeFlow();
  }

  /**
   * Logout from OAuth2 provider
   */
  public logout(): void {
    this.oauthService.logOut(true);
    this.userSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Refresh the current token
   */
  public refreshToken(): Observable<boolean> {
    return from(this.oauthService.refreshToken().then(() => {
      this.updateAuthenticationStatus();
      return true;
    })).pipe(
      catchError(error => {
        console.error('Token refresh failed:', error);
        return of(false);
      })
    );
  }

  /**
   * Load user profile information
   */
  public loadUserProfile(): Observable<OAuthUser | null> {
    if (!this.oauthService.hasValidAccessToken()) {
      return of(null);
    }

    return from(this.oauthService.loadUserProfile()).pipe(
      map((profile: any) => {
        const user = this.mapUserProfile(profile);
        this.userSubject.next(user);
        return user;
      }),
      catchError(error => {
        console.error('Failed to load user profile:', error);
        return of(null);
      })
    );
  }

  /**
   * Get the current access token
   */
  public getAccessToken(): string | null {
    return this.oauthService.getAccessToken();
  }

  /**
   * Get the current ID token (if available)
   */
  public getIdToken(): string | null {
    return this.oauthService.getIdToken();
  }

  /**
   * Get the refresh token (if available)
   */
  public getRefreshToken(): string | null {
    return this.oauthService.getRefreshToken();
  }

  /**
   * Check if the user is authenticated
   */
  public isAuthenticated(): boolean {
    return this.oauthService.hasValidAccessToken();
  }

  /**
   * Get the current access token expiration time
   */
  public getAccessTokenExpiration(): number | null {
    const expiration = this.oauthService.getAccessTokenExpiration();
    return expiration ? expiration * 1000 : null;
  }

  /**
   * Map OAuth2 user profile to application user model
   */
  private mapUserProfile(profile: any): OAuthUser {
    return {
      id: profile.sub || profile.id || '',
      email: profile.email || '',
      name: profile.name || profile.given_name || '',
      picture: profile.picture || '',
      ...profile
    };
  }

  /**
   * Update authentication status and token expiration
   */
  private updateAuthenticationStatus(): void {
    const isAuthenticated = this.oauthService.hasValidAccessToken();
    this.isAuthenticatedSubject.next(isAuthenticated);

    if (isAuthenticated) {
      this.tokenExpirationSubject.next(this.getAccessTokenExpiration());
    } else {
      this.tokenExpirationSubject.next(null);
    }
  }

  /**
   * Request user info from the userinfo endpoint
   */
  public getUserInfo(): Observable<any> {
    const token = this.getAccessToken();
    if (!token) {
      return of(null);
    }

    const headers = new HttpHeaders({
      Authorization: `Bearer ${token}`
    });

    return this.http.get(this.oauthService.userinfoEndpoint || '', { headers }).pipe(
      tap(userInfo => {
        const user = this.mapUserProfile(userInfo);
        this.userSubject.next(user);
      }),
      catchError(error => {
        console.error('Failed to get user info:', error);
        return of(null);
      })
    );
  }

  /**
   * Silent refresh setup
   */
  public startSilentRefresh(): void {
    this.oauthService.setupAutomaticSilentRefresh();
  }

  /**
   * Get the authorization header value for HTTP requests
   */
  public getAuthorizationHeader(): string | null {
    const token = this.getAccessToken();
    return token ? `Bearer ${token}` : null;
  }

  /**
   * Get the on-behalf-of header value for backend requests.
   * This allows the backend to use the user's access token
   * when calling downstream APIs on behalf of the user.
   */
  public getOnBehalfOfHeader(): { [header: string]: string } | null {
    const token = this.getAccessToken();
    return token ? { 'X-On-Behalf-Of': `Bearer ${token}` } : null;
  }
}
