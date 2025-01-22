import { AnimationsService, LocalStorageService } from '@app/core';
import { Actions, getEffectsMetadata } from '@ngrx/effects';
import { cold } from 'jasmine-marbles';
import { EMPTY } from 'rxjs';
import { ActionMainPersist } from './main.actions';
import { MainEffects, MAIN_KEY } from './main.effects';
import { MainState } from './main.model';
import { MainService } from './main.service';

describe('MainEffects', () => {
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let animationsService: jasmine.SpyObj<AnimationsService>;
  let mainService: jasmine.SpyObj<MainService>;

  beforeEach(() => {
    localStorageService = jasmine.createSpyObj('LocalStorageService', [
      'setItem'
    ]);
    animationsService = jasmine.createSpyObj('AnimationsService', [
      'updateRouteAnimationType'
    ]);
    /*
    mainService = jasmine.createSpyObj('MainService', [
      'updateRouteAnimationType'
    ]);
    */
  });
/*
  describe('persistMain', () => {
    it('should not dispatch any action', () => {
      const actions = new Actions(EMPTY);
      const effect = new MainEffects(
        actions,
        localStorageService,
        animationsService,
        mainService
      );
      const metadata = getEffectsMetadata(effect);

      expect(metadata.persistMain).toEqual({ dispatch: false });
    });
  });

  it('should call methods on AnimationsService and LocalStorageService for PERSIST action', () => {
    const main: MainState = {
      language: 'en',
      pageAnimations: true,
      elementsAnimations: true,
      theme: 'default',
      autoNightMode: false,
      stickyHeader: false,
      pageAnimationsDisabled: true
    };
    const persistAction = new ActionMainPersist({ main: main });
    const source = cold('a', { a: persistAction });
    const actions = new Actions(source);
    const effect = new MainEffects(
      actions,
      localStorageService,
      animationsService,
      mainService
    );

    effect.persistMain.subscribe(() => {
      expect(localStorageService.setItem).toHaveBeenCalledWith(
        MAIN_KEY,
        persistAction.payload.main
      );
      expect(animationsService.updateRouteAnimationType).toHaveBeenCalledWith(
        true,
        true
      );
    });
  });
  */
});
