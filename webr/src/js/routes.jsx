import React from 'react';
import {
  useLocation,
  useNavigate,
  useParams,
  BrowserRouter,
  Routes,
  Route,
} from 'react-router-dom';
import Header from './common/components/Header/Header';
import Login from './common/components/Auth/Login';
import OAuthCallback from './common/components/Auth/OAuthCallback';
import ProtectedRoute from './common/components/Auth/ProtectedRoute';
import MainRouteHandler from './views/main';

const JustAnotherPage = () => (
  <div>
    <h2>This is Just Another Page</h2>
    <p>Please remove this from your route, it is just to show case basic setup for router.</p>
  </div>
);

const HeaderWithRouter = withRouter(props => <Header {...props} />);

function withRouter(Component) {
  function ComponentWithRouterProp(props) {
    let location = useLocation();
    let navigate = useNavigate();
    let params = useParams();
    return (
      <Component
        {...props}
        router={{ location, navigate, params }}
      />
    );
  }

  return ComponentWithRouterProp;
}

const amodule = (
  <div className="container">
    <HeaderWithRouter />
    <hr />
      <div className="container__content">
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/auth/callback" element={<OAuthCallback />} />
        
        {/* Protected routes */}
        <Route exact path="/" element={
          <ProtectedRoute>
            <MainRouteHandler/>
          </ProtectedRoute>
        } />
        <Route path="/page" element={
          <ProtectedRoute>
            <JustAnotherPage/>
          </ProtectedRoute>
        } />
        
        {/* Catch-all route */}
        <Route path="*" element={
          <ProtectedRoute>
            <MainRouteHandler/>
          </ProtectedRoute>
        } />
      </Routes>
    </div>
  </div>
);

export default amodule;
