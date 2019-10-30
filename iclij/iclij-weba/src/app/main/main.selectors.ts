import { createFeatureSelector, createSelector } from '@ngrx/store';

import { MainState, State } from './main.model';

export const selectMainState = createFeatureSelector<State, MainState>(
  'main'
);

export const selectMain = createSelector(
  selectMainState,
  (state: MainState) => state
);

export const selectCount = createSelector(
  selectMainState,
  (state: MainState) => state.count
);

export const selectTabs = createSelector(
  selectMainState,
  (state: MainState) => state.tabs
);

export const selectStartdate = createSelector(
  selectMainState,
  (state: MainState) => state.startdate
);

export const selectEnddate = createSelector(
  selectMainState,
  (state: MainState) => state.enddate
);

export const selectMarket = createSelector(
  selectMainState,
  (state: MainState) => state.market
);

export const selectMarkets = createSelector(
  selectMainState,
  (state: MainState) => state.markets
);

export const selectConfig = createSelector(
  selectMainState,
  (state: MainState) => state.config
);
