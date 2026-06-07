import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { Nav, Navbar, Button, Container, Dropdown } from 'react-bootstrap';
import { selectIsAuthenticated, selectUser } from '../../../redux/selectors/authSelector';

import './Header.css';

const Header = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const isAuthenticated = useSelector(selectIsAuthenticated);
  const user = useSelector(selectUser);

  const handleLogout = () => {
    dispatch({ type: 'auth/logout' });
    navigate('/login');
  };

  const handleLogin = () => {
    // Initiate OAuth2 Authorization Code + PKCE flow
    dispatch({ type: 'auth/initiateAuthFlow' });
  };

  return (
    <header className="globalHeader">
      <Navbar bg="light" expand="lg" sticky="top">
        <Container>
          <Navbar.Brand as={Link} to="/">
            Stock Stats Spark
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="basic-navbar-nav" />
          <Navbar.Collapse id="basic-navbar-nav">
            <Nav className="ms-auto">
              {isAuthenticated ? (
                <>
                  <Dropdown>
                    <Dropdown.Toggle variant="link" className="nav-link">
                      {user?.name || user?.email || 'User'}
                    </Dropdown.Toggle>
                    <Dropdown.Menu align="end">
                      <Dropdown.Item disabled>
                        {user?.email && `Email: ${user.email}`}
                      </Dropdown.Item>
                      <Dropdown.Divider />
                      <Dropdown.Item onClick={handleLogout}>
                        Sign Out
                      </Dropdown.Item>
                    </Dropdown.Menu>
                  </Dropdown>
                </>
              ) : (
                <Button variant="primary" onClick={handleLogin}>
                  Sign In
                </Button>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
    </header>
  );
};

export default Header;
