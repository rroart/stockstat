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

export const mainSelector = state => ({
    result2: resultSelector(state),
    result3: resultSelector3(state),
    result4: resultSelector4(state),
    count: resultCount(state),
    tabs: resultTabs(state),
});
