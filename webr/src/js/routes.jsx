import React from 'react';
import {
  useLocation,
  useNavigate,
  useParams,
  BrowserRouter,
  Routes,
  Route,
} from 'react-router-dom';
import { Header } from './common/components/Header';
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
        <Route exact path="/" element={<MainRouteHandler/>} />
        <Route path="/page" component={<JustAnotherPage/>} />
          <Route path="*" component={<MainRouteHandler/>} />
      </Routes>
    </div>
  </div>
);

export default amodule;
