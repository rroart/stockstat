/**
 * Login Component
 * 
 * Provides login UI and initiates OAuth2 flow
 */

import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { Button, Container, Row, Col, Card, Alert, Spinner } from 'react-bootstrap';
import { selectIsAuthenticated, selectIsLoading, selectAuthError } from '../../../redux/selectors/authSelector';

interface LoginProps {
  redirectTo?: string;
}

const Login: React.FC<LoginProps> = ({ redirectTo = '/' }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const isLoading = useSelector(selectIsLoading);
  const error = useSelector(selectAuthError);

  useEffect(() => {
    // Redirect if already authenticated
    if (isAuthenticated) {
      navigate(redirectTo);
    }
  }, [isAuthenticated, navigate, redirectTo]);

  const handleLogin = () => {
    dispatch({ type: 'auth/initiateAuthFlow' });
  };

  return (
    <Container className="d-flex align-items-center justify-content-center" style={{ minHeight: '100vh' }}>
      <Row className="w-100">
        <Col xs={12} sm={10} md={6} lg={4} className="mx-auto">
          <Card>
            <Card.Body className="p-4">
              <Card.Title className="text-center mb-4">
                <h2>Login</h2>
              </Card.Title>

              {error && (
                <Alert variant="danger" dismissible onClose={() => dispatch({ type: 'auth/clearError' })}>
                  <strong>Authentication Error:</strong> {error}
                </Alert>
              )}

              <p className="text-center text-muted mb-4">
                Please log in with your account to continue.
              </p>

              <Button
                variant="primary"
                size="lg"
                className="w-100"
                onClick={handleLogin}
                disabled={isLoading}
              >
                {isLoading ? (
                  <>
                    <Spinner as="span" animation="border" size="sm" role="status" aria-hidden="true" className="me-2" />
                    Redirecting to login...
                  </>
                ) : (
                  'Sign In with OAuth2'
                )}
              </Button>

              <hr className="my-3" />

              <p className="text-center text-muted small">
                This application uses OAuth2 for secure authentication. You will be redirected to the login provider.
              </p>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default Login;
