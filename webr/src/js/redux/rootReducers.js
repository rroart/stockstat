import { combineReducers } from 'redux';
import { routerReducer as routing } from 'react-router-redux';
import main from './modules/main';
import authReducer from './modules/auth';

export default combineReducers({
  main,
  routing,
  auth: authReducer,
});
