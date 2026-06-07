/**
 * OAuth2 Configuration
 * 
 * This configuration supports Authorization Code Flow with PKCE (RFC 7636)
 * which is the recommended flow for single-page applications.
 */

export interface OAuth2Config {
  clientId: string;
  clientSecret?: string;
  authorizationEndpoint: string;
  tokenEndpoint: string;
  redirectUri: string;
  scope: string;
  audience?: string;
}

/**
 * Default OAuth2 configuration
 * These values should be overridden via environment variables or config files
 */
const defaultConfig: OAuth2Config = {
  clientId: process.env.REACT_APP_OAUTH2_CLIENT_ID || 'your-client-id',
  clientSecret: process.env.REACT_APP_OAUTH2_CLIENT_SECRET || undefined,
  authorizationEndpoint: process.env.REACT_APP_OAUTH2_AUTH_ENDPOINT || 'https://oauth.example.com/authorize',
  tokenEndpoint: process.env.REACT_APP_OAUTH2_TOKEN_ENDPOINT || 'https://oauth.example.com/token',
  redirectUri: process.env.REACT_APP_OAUTH2_REDIRECT_URI || `${window.location.origin}/auth/callback`,
  scope: process.env.REACT_APP_OAUTH2_SCOPE || 'openid profile email',
  audience: process.env.REACT_APP_OAUTH2_AUDIENCE || undefined,
};

export default defaultConfig;

/**
 * PKCE Helper Functions
 */
export const generateCodeVerifier = (): string => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_';
  const length = 128;
  let result = '';
  const randomValues = new Uint8Array(length);
  crypto.getRandomValues(randomValues);
  for (let i = 0; i < length; i++) {
    result += chars[randomValues[i] % chars.length];
  }
  return result;
};

export const generateCodeChallenge = async (verifier: string): Promise<string> => {
  const encoder = new TextEncoder();
  const data = encoder.encode(verifier);
  const hash = await crypto.subtle.digest('SHA-256', data);
  const hashArray = Array.from(new Uint8Array(hash));
  const hashString = hashArray.map((b) => String.fromCharCode(b)).join('');
  return btoa(hashString).replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
};

/**
 * Generate OAuth2 authorization URL
 */
export const generateAuthorizationUrl = async (config: OAuth2Config): Promise<string> => {
  const verifier = generateCodeVerifier();
  const challenge = await generateCodeChallenge(verifier);
  
  // Store verifier in session storage for later exchange
  sessionStorage.setItem('oauth2_code_verifier', verifier);
  
  const params = new URLSearchParams({
    client_id: config.clientId,
    redirect_uri: config.redirectUri,
    response_type: 'code',
    scope: config.scope,
    code_challenge: challenge,
    code_challenge_method: 'S256',
    state: generateRandomState(),
  });
  
  if (config.audience) {
    params.append('audience', config.audience);
  }
  
  return `${config.authorizationEndpoint}?${params.toString()}`;
};

/**
 * Generate random state for CSRF protection
 */
export const generateRandomState = (): string => {
  const randomValues = new Uint8Array(32);
  crypto.getRandomValues(randomValues);
  return Array.from(randomValues)
    .map((b) => b.toString(16).padStart(2, '0'))
    .join('');
};

/**
 * Exchange authorization code for access token
 */
export const exchangeCodeForToken = async (
  config: OAuth2Config,
  code: string,
  state: string
): Promise<{ access_token: string; refresh_token?: string; expires_in?: number; token_type: string }> => {
  const verifier = sessionStorage.getItem('oauth2_code_verifier');
  
  if (!verifier) {
    throw new Error('Code verifier not found. Session may have expired.');
  }
  
  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code,
    client_id: config.clientId,
    redirect_uri: config.redirectUri,
    code_verifier: verifier,
  });
  
  if (config.clientSecret) {
    body.append('client_secret', config.clientSecret);
  }
  
  const response = await fetch(config.tokenEndpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body.toString(),
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(`Token exchange failed: ${error.error || response.statusText}`);
  }
  
  // Clear the verifier after successful exchange
  sessionStorage.removeItem('oauth2_code_verifier');
  
  return response.json();
};

/**
 * Refresh access token using refresh token
 */
export const refreshAccessToken = async (
  config: OAuth2Config,
  refreshToken: string
): Promise<{ access_token: string; refresh_token?: string; expires_in?: number; token_type: string }> => {
  const body = new URLSearchParams({
    grant_type: 'refresh_token',
    refresh_token: refreshToken,
    client_id: config.clientId,
  });
  
  if (config.clientSecret) {
    body.append('client_secret', config.clientSecret);
  }
  
  const response = await fetch(config.tokenEndpoint, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: body.toString(),
  });
  
  if (!response.ok) {
    throw new Error('Token refresh failed');
  }
  
  return response.json();
};
