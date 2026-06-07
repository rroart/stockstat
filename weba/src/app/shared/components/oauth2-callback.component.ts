import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Store } from '@ngrx/store';

import { OAuth2Service } from '@app/core/auth/oauth2.service';
import { OAuthUser } from '@app/core/auth/auth.models';
import { AppState } from '@app/core/core.state';
import { ActionOAuth2LoginSuccess, ActionOAuth2LoginFailure } from '@app/core/auth/auth.actions';

/**
 * OAuth2 Callback Component
 * Handles the OAuth2 redirect callback after user authorization
 * This component is used as the redirect_uri for OAuth2 flows
 */
@Component({
  selector: 'app-oauth-callback',
  template: `
    <div class="oauth-callback-container">
      <div class="spinner">
        <div class="spinner-border" role="status">
          <span class="sr-only">Loading...</span>
        </div>
      </div>
      <p>{{ message }}</p>
    </div>
  `,
  styles: [`
    .oauth-callback-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    .spinner {
      margin-bottom: 20px;
    }
    p {
      color: white;
      font-size: 16px;
      margin-top: 20px;
    }
  `]
})
export class OAuth2CallbackComponent implements OnInit {
  message = 'Authenticating...';

  constructor(
    private oauth2Service: OAuth2Service,
    private store: Store<AppState>,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.handleCallback();
  }

  private handleCallback(): void {
    try {
      // The OAuthService handles token extraction automatically if the login flow redirected here.
      if (this.oauth2Service.isAuthenticated()) {
        this.message = 'Loading user profile...';

        this.oauth2Service.loadUserProfile().subscribe(
          (user: OAuthUser | null) => {
            if (user) {
              const token = {
                accessToken: this.oauth2Service.getAccessToken() || '',
                idToken: this.oauth2Service.getIdToken() || undefined,
                refreshToken: this.oauth2Service.getRefreshToken() || undefined,
                expiresIn: this.oauth2Service.getAccessTokenExpiration() || undefined
              };
              this.store.dispatch(new ActionOAuth2LoginSuccess({ user, token }));
              this.router.navigate(['/']);
            } else {
              this.handleError('Failed to load user profile');
            }
          },
          error => {
            this.handleError(error?.message || 'Authentication failed');
          }
        );
      } else {
        this.handleError('Authentication failed: No valid token');
      }
    } catch (error) {
      console.error('OAuth2 callback error:', error);
      this.handleError(error instanceof Error ? error.message : 'Authentication error');
    }
  }

  private handleError(errorMessage: string): void {
    console.error(errorMessage);
    this.message = 'Authentication failed. Redirecting...';
    this.store.dispatch(new ActionOAuth2LoginFailure({ error: errorMessage }));
    setTimeout(() => {
      this.router.navigate(['/']);
    }, 2000);
  }
}
