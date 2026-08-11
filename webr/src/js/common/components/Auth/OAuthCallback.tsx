/**
 * OAuth Callback Handler Component
 * 
 * This component handles the OAuth2 callback from the authentication provider.
 * It extracts the authorization code from the URL and dispatches the
 * token exchange action.
 */

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Container, Spinner, Alert } from 'react-bootstrap';
import { selectIsLoading, selectAuthError, selectIsAuthenticated } from '../../../redux/selectors/authSelector';

interface OAuthCallbackProps {
  redirectTo?: string;
}

const OAuthCallback: React.FC<OAuthCallbackProps> = ({ redirectTo = '/' }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const isLoading = useSelector(selectIsLoading);
  const error = useSelector(selectAuthError);
  const isAuthenticated = useSelector(selectIsAuthenticated);

  useEffect(() => {
    // Extract code and state from URL
    const code = searchParams.get('code');
    const state = searchParams.get('state');
    const errorParam = searchParams.get('error');
    const errorDescription = searchParams.get('error_description');

    // Handle error from OAuth provider
    if (errorParam) {
      console.error('OAuth error:', errorParam, errorDescription);
      // Dispatch error action or redirect to login with error message
      navigate(`/login?error=${encodeURIComponent(errorParam)}`);
      return;
    }

    // If we have a code, dispatch the callback action
    if (code) {
      dispatch({
        type: 'auth/handleAuthCallback',
        payload: { code, state: state || '' },
      });
    } else {
      console.error('No authorization code received from OAuth provider');
      navigate('/login?error=No authorization code received');
    }
  }, [dispatch, navigate, searchParams]);

  // Redirect on successful authentication
  useEffect(() => {
    if (isAuthenticated && !isLoading) {
      navigate(redirectTo, { replace: true });
    }
  }, [isAuthenticated, isLoading, navigate, redirectTo]);

  return (
    <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh' }}>
      <div className="text-center">
        {error ? (
          <Alert variant="danger">
            <h4>Authentication Error</h4>
            <p>{error}</p>
          </Alert>
        ) : (
          <>
            <Spinner animation="border" role="status" variant="primary">
              <span className="visually-hidden">Loading...</span>
            </Spinner>
            <p className="mt-3">Processing your login...</p>
          </>
        )}
      </div>
    </Container>
  );
};

export default OAuthCallback;
