/**
 * OAuth2 Authentication Models
 * Defines TypeScript interfaces for OAuth2 user, token, and authentication state
 */

/**
 * Supported authentication providers
 */
export enum AuthProvider {
  OAUTH2 = 'oauth2',
  LOCAL = 'local',
  NONE = 'none'
}

/**
 * OAuth2 User Profile Information
 */
export interface OAuthUser {
  /** Unique user identifier from OAuth2 provider */
  id: string;
  /** User email address */
  email: string;
  /** User display name */
  name: string;
  /** User avatar/profile picture URL (optional) */
  picture?: string;
  /** Additional user properties from OAuth2 provider */
  [key: string]: any;
}

/**
 * OAuth2 Token Information
 */
export interface OAuthToken {
  /** Access token for API requests */
  accessToken: string;
  /** OpenID Connect ID token (optional) */
  idToken?: string;
  /** Refresh token for obtaining new access tokens (optional) */
  refreshToken?: string;
  /** Token lifetime in seconds */
  expiresIn?: number;
  /** Absolute expiration timestamp in milliseconds */
  expiresAt?: number;
  /** Token type (typically 'Bearer') */
  tokenType?: string;
  /** Additional token scope information */
  scope?: string;
}

/**
 * Authentication State
 * Represents the current authentication status and user information
 */
export interface AuthState {
  /** Whether user is currently authenticated */
  isAuthenticated: boolean;
  /** Current authenticated user (null if not authenticated) */
  user: OAuthUser | null;
  /** Current authentication token (null if not authenticated) */
  token: OAuthToken | null;
  /** Whether authentication is in progress */
  loading: boolean;
  /** Error message if authentication failed */
  error: string | null;
  /** Authentication provider used ('oauth2', 'local', 'none') */
  provider: AuthProvider;
  /** Last successful authentication timestamp */
  lastAuthTime?: number;
  /** Whether token refresh is currently in progress */
  refreshingToken?: boolean;
}

/**
 * Helper type for OAuth2 configuration from environment
 */
export interface OAuth2Environment {
  clientId: string;
  issuer: string;
  loginUrl?: string;
  tokenEndpoint?: string;
  userinfoEndpoint?: string;
  logoutUrl?: string;
  scope?: string;
  responseType?: string;
  redirectUri?: string;
  [key: string]: any;
}

/**
 * Authentication event/payload for logging and debugging
 */
export interface AuthEvent {
  type: 'login' | 'logout' | 'refresh' | 'error' | 'initialize';
  timestamp: number;
  provider?: AuthProvider;
  error?: string;
  metadata?: Record<string, any>;
}

