import { combineReducers } from 'redux';
import { routerReducer as routing } from 'react-router-redux';
import main from './modules/main';

export default combineReducers({
  main,
  routing,
});
