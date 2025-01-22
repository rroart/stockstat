import { initialState, mainReducer } from './main.reducer';

import {
  ActionMainChangeAnimationsElements,
  ActionMainChangeAnimationsPage,
  ActionMainChangeAnimationsPageDisabled,
  ActionMainChangeAutoNightMode,
  ActionMainChangeLanguage,
  ActionMainChangeTheme,
  ActionMainChangeStickyHeader
} from './main.actions';

describe('MainReducer', () => {
  it('should return default state', () => {
    const action = {} as any;
    const state = mainReducer(undefined, action);
    expect(state).toBe(initialState);
  });

  /*
  it('should update language', () => {
    const action = new ActionMainChangeLanguage({ language: 'sk' });
    const state = mainReducer(undefined, action);
    expect(state.language).toEqual('sk');
  });

  it('should update theme', () => {
    const action = new ActionMainChangeTheme({ theme: 'dark' });
    const state = mainReducer(undefined, action);
    expect(state.theme).toEqual('dark');
  });

  it('should update pageAnimations', () => {
    const action = new ActionMainChangeAnimationsPage({
      pageAnimations: false
    });
    const state = mainReducer(undefined, action);
    expect(state.pageAnimations).toEqual(false);
  });

  it('should update pageAnimationsDisabled and pageAnimations', () => {
    const action = new ActionMainChangeAnimationsPageDisabled({
      pageAnimationsDisabled: true
    });
    const state = mainReducer(undefined, action);
    expect(state.pageAnimationsDisabled).toEqual(true);
    expect(state.pageAnimations).toEqual(false);
  });

  it('should update elementsAnimations', () => {
    const action = new ActionMainChangeAnimationsElements({
      elementsAnimations: false
    });
    const state = mainReducer(undefined, action);
    expect(state.elementsAnimations).toEqual(false);
  });

  it('should update autoNightMode', () => {
    const action = new ActionMainChangeAutoNightMode({
      autoNightMode: true
    });
    const state = mainReducer(undefined, action);
    expect(state.autoNightMode).toEqual(true);
  });

  it('should update stickyHeader', () => {
    const action = new ActionMainChangeStickyHeader({
      stickyHeader: false
    });
    const state = mainReducer(undefined, action);
    expect(state.stickyHeader).toEqual(false);
  });
  */
});
