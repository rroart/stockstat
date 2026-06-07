import { AuthConfig } from 'angular-oauth2-oidc';
import { environment } from '@env/environment';

interface OAuth2Environment extends Partial<AuthConfig> {
  googleClientId?: string;
  azureClientId?: string;
  azureTenantId?: string;
  githubClientId?: string;
}

const oauth2Env = ((environment as any).oauth2 || {}) as OAuth2Environment;

/**
 * OAuth2 Configuration for your application
 * Update these values with your OAuth2 provider's credentials
 */
export const oauth2Config: AuthConfig = {
  clientId: oauth2Env.clientId || 'your-client-id',
  redirectUri: oauth2Env.redirectUri || window.location.origin + '/#/callback',
  silentRefreshRedirectUri:
    oauth2Env.silentRefreshRedirectUri || window.location.origin + '/silent-refresh.html',

  // Server endpoint configurations
  loginUrl: oauth2Env.loginUrl || 'https://your-oauth-provider.com/oauth/authorize',
  tokenEndpoint: oauth2Env.tokenEndpoint || 'https://your-oauth-provider.com/oauth/token',
  userinfoEndpoint:
    oauth2Env.userinfoEndpoint || 'https://your-oauth-provider.com/oauth/userinfo',
  logoutUrl: oauth2Env.logoutUrl || 'https://your-oauth-provider.com/oauth/logout',

  // OIDC specific (optional)
  issuer: oauth2Env.issuer,
  oidc: oauth2Env.oidc !== undefined ? oauth2Env.oidc : true,

  // Scopes to request
  scope: oauth2Env.scope || 'openid profile email',
  responseType: oauth2Env.responseType || 'code',

  // Silent refresh settings
  useSilentRefresh: true,
  silentRefreshTimeout: oauth2Env.silentRefreshTimeout || 5 * 60 * 1000,
  sessionChecksEnabled: true,
  sessionCheckIntervall: oauth2Env.sessionCheckIntervall || 5 * 60 * 1000,

  // Token validation
  strictDiscoveryDocumentValidation:
    oauth2Env.strictDiscoveryDocumentValidation !== undefined
      ? oauth2Env.strictDiscoveryDocumentValidation
      : false,
  requireHttps: oauth2Env.requireHttps !== undefined ? oauth2Env.requireHttps : false,
  timeoutFactor: oauth2Env.timeoutFactor || 0.75,
  showDebugInformation: !environment.production
};

/**
 * Example configurations for popular OAuth2 providers
 * Uncomment and customize as needed
 */

export const googleOAuth2Config: AuthConfig = {
  ...oauth2Config,
  clientId:
    oauth2Env.googleClientId || 'your-google-client-id.apps.googleusercontent.com',
  loginUrl: 'https://accounts.google.com/o/oauth2/v2/auth',
  tokenEndpoint: 'https://oauth2.googleapis.com/token',
  userinfoEndpoint: 'https://www.googleapis.com/oauth2/v2/userinfo',
  redirectUri: window.location.origin + '/#/callback',
  scope: 'openid profile email'
};

export const azureADConfig: AuthConfig = {
  ...oauth2Config,
  clientId: oauth2Env.azureClientId || 'your-azure-client-id',
  loginUrl: `https://login.microsoftonline.com/${oauth2Env.azureTenantId || 'common'}/oauth2/v2.0/authorize`,
  tokenEndpoint: `https://login.microsoftonline.com/${oauth2Env.azureTenantId || 'common'}/oauth2/v2.0/token`,
  userinfoEndpoint: 'https://graph.microsoft.com/v1.0/me',
  redirectUri: window.location.origin + '/#/callback',
  scope: 'openid profile email'
};

export const githubOAuth2Config: AuthConfig = {
  ...oauth2Config,
  clientId: oauth2Env.githubClientId || 'your-github-client-id',
  loginUrl: 'https://github.com/login/oauth/authorize',
  tokenEndpoint: 'https://github.com/login/oauth/access_token',
  userinfoEndpoint: 'https://api.github.com/user',
  redirectUri: window.location.origin + '/#/callback',
  scope: 'user:email'
};
