import { reducers, constants, actions, initialState } from  '../../../src/js/redux/modules/main'
import { getStoreOld } from '../../../__fixtures__/store'
import { configureStore, EnhancedStore } from '@reduxjs/toolkit'
import { Store } from 'redux'
import main from '../../../src/js/redux/modules/main';
import { Map, MapOf } from 'immutable';

const fixture = {
  title: 'fake-title',
  description: 'fake-description',
  source: 'fake-source',
}

/*
describe('redux.modules.main', () => {
  var store : EnhancedStore;
 
  beforeEach(() => {
    store = configureStore({
      reducer : main
    });
  });

  afterEach(() => {
    // TODO store = null;
  })

  it('should return correct state when running updateExample', () => {
    const type = constants.UPDATE_MAIN
    const state = store.getState().main

    const result = reducers[type](state, { payload: fixture })

    expect(result.get('source')).toEqual(fixture.source)
    expect(result.get('title')).toEqual(fixture.title)
  })
})
*/

describe('redux.modules.main', () => {
  var store : Store;

  beforeEach(() => {
    console.log("herehere");
    store = getStoreOld({
      main: initialState()
    });
  });

  afterEach(() => {
    // TODO store = null;
  })

  it('should return correct state when running updateExample', () => {
    const type = constants.UPDATE_MAIN
    const state = store.getState().main

    const result = reducers[type](state, { payload: fixture })

    expect(result.get('source')).toEqual(fixture.source)
    expect(result.get('title')).toEqual(fixture.title)
  })
})
