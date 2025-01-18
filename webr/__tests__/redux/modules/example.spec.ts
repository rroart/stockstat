import { reducers, constants, actions, initialState } from  '../../../src/js/redux/modules/main'
import { getStore } from '../../../__fixtures__/store'

const fixture = {
  title: 'fake-title',
  description: 'fake-description',
  source: 'fake-source',
}

describe('redux.modules.main', () => {
  let store = null;

  beforeEach(() => {
    store = getStore({
      main: initialState()
    });
  });

  afterEach(() => {
    store = null;
  })

  it('should return correct state when running updateExample', () => {
    const type = constants.UPDATE_EXAMPLE
    const state = store.getState().main

    const result = reducers[type](state, { payload: fixture })

    expect(result.get('source')).toEqual(fixture.source)
    expect(result.get('title')).toEqual(fixture.title)
  })
})
