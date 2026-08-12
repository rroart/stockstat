import { selectAuth, selectIsAuthenticated } from './auth.selectors';
import { AuthProvider } from './auth.models';

describe('Auth Selectors', () => {
  it('selectAuth', () => {
    const state = createAuthState();
    expect(selectAuth(state)).toBe(state.auth);
  });

  it('selectIsAuthenticated', () => {
    const state = createAuthState();
    expect(selectIsAuthenticated(state)).toBe(false);
  });
});

function createAuthState() {
  return {
    auth: {
      isAuthenticated: false,
      user: null,
      token: null,
      loading: false,
      error: null,
      provider: AuthProvider.NONE,
      lastAuthTime: undefined,
      refreshingToken: false
    },
    router: {} as any
  };
}
