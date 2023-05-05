import { createSelector } from 'reselect';

const mainDataSelector = state => state.main;

const resultSelector = createSelector(
  mainDataSelector,
  payload => payload.get('result2')
);

const resultSelector3 = createSelector(
  mainDataSelector,
  payload => payload.get('result3')
);

const resultSelector4 = createSelector(
  mainDataSelector,
  payload => payload.get('result4')
);

const resultCount = createSelector(
  mainDataSelector,
  payload => payload.get('count')
);

const resultTabs = createSelector(
  mainDataSelector,
  payload => payload.get('tabs')
);

const resultStartDate = createSelector(
  mainDataSelector,
  payload => payload.get('startdate')
);

const resultEndDate = createSelector(
  mainDataSelector,
  payload => payload.get('enddate')
);

const resultMarket = createSelector(
  mainDataSelector,
  payload => payload.get('market')
);

const resultIMarket = createSelector(
  mainDataSelector,
  payload => payload.get('imarket')
);

const resultMarkets = createSelector(
  mainDataSelector,
  payload => payload.get('markets')
);

const resultConfig = createSelector(
  mainDataSelector,
  payload => payload.get('config')
);

const resultIConfig = createSelector(
  mainDataSelector,
  payload => payload.get('iconfig')
);

export const mainSelector = state => ({
    result2: resultSelector(state),
    result3: resultSelector3(state),
    result4: resultSelector4(state),
    count: resultCount(state),
    tabs: resultTabs(state),
    startdate: resultStartDate(state),
    enddate: resultEndDate(state),
    market: resultMarket(state),
    imarket: resultIMarket(state),
    markets: resultMarkets(state),
    config: resultConfig(state),
    iconfig: resultIConfig(state),
});
