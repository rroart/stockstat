import {
  ActionMainChangeAnimationsElements,
  ActionMainChangeAnimationsPage,
  ActionMainChangeAnimationsPageDisabled,
  ActionMainChangeAutoNightMode,
  ActionMainChangeLanguage,
  ActionMainChangeStickyHeader,
  ActionMainChangeTheme,
  ActionMainPersist,
  MainActionTypes
} from './main.actions';
import { NIGHT_MODE_THEME } from './main.model';

describe('Main Actions', () => {
  it('should create ActionMainChangeTheme action', () => {
    const action = new ActionMainChangeTheme({
      theme: NIGHT_MODE_THEME
    });

    expect(action.type).toEqual(MainActionTypes.CHANGE_THEME);
    expect(action.payload.theme).toEqual(NIGHT_MODE_THEME);
  });

  it('should create ActionMainChangeAnimationsElements action', () => {
    const action = new ActionMainChangeAnimationsElements({
      elementsAnimations: true
    });

    expect(action.type).toEqual(MainActionTypes.CHANGE_ANIMATIONS_ELEMENTS);
    expect(action.payload.elementsAnimations).toEqual(true);
  });

  it('should create ActionMainChangeAnimationsPage action', () => {
    const action = new ActionMainChangeAnimationsPage({
      pageAnimations: true
    });

    expect(action.type).toEqual(MainActionTypes.CHANGE_ANIMATIONS_PAGE);
    expect(action.payload.pageAnimations).toEqual(true);
  });

  it('should create ActionMainChangeAnimationsPageDisabled action', () => {
    const action = new ActionMainChangeAnimationsPageDisabled({
      pageAnimationsDisabled: true
    });

    expect(action.type).toEqual(
      MainActionTypes.CHANGE_ANIMATIONS_PAGE_DISABLED
    );
    expect(action.payload.pageAnimationsDisabled).toEqual(true);
  });

  it('should create ActionMainChangeAutoNightMode action', () => {
    const action = new ActionMainChangeAutoNightMode({
      autoNightMode: true
    });

    expect(action.type).toEqual(
      MainActionTypes.CHANGE_AUTO_NIGHT_AUTO_MODE
    );
    expect(action.payload.autoNightMode).toEqual(true);
  });

  it('should create ActionMainChangeLanguage action', () => {
    const action = new ActionMainChangeLanguage({
      language: 'en'
    });

    expect(action.type).toEqual(MainActionTypes.CHANGE_LANGUAGE);
    expect(action.payload.language).toEqual('en');
  });

  it('should create ActionMainChangeStickyHeader action', () => {
    const action = new ActionMainChangeStickyHeader({
      stickyHeader: true
    });

    expect(action.type).toEqual(MainActionTypes.CHANGE_STICKY_HEADER);
    expect(action.payload.stickyHeader).toEqual(true);
  });

  /*
  it('should create ActionMainPersist action', () => {
    const action = new ActionMainPersist({
      main: {
        count: 0,
        tabs: undefined,
        startdate: '',
        enddate: '',
        market: '',
        market2: '',
        markets: [],
        tasks: [],
        config: undefined,
        config2: undefined
      }
    });

    expect(action.type).toEqual(MainActionTypes.PERSIST);
    expect(action.payload.main).toEqual(
      jasmine.objectContaining({
        autoNightMode: true,
        elementsAnimations: true,
        language: 'en',
        theme: NIGHT_MODE_THEME,
        pageAnimations: true,
        pageAnimationsDisabled: true,
        stickyHeader: true
      })
    );
  });
  */
});
