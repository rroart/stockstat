import { Action } from '@ngrx/store';

import { HttpErrorResponse } from '@angular/common/http';

import { Language, MainState } from './main.model';

export enum MainActionTypes {
  CHANGE_LANGUAGE = '[Main] Change Language',
  CHANGE_THEME = '[Main] Change Theme',
  CHANGE_AUTO_NIGHT_AUTO_MODE = '[Main] Change Auto Night Mode',
  CHANGE_STICKY_HEADER = '[Main] Change Sticky Header',
  INCREMENT = '[Main] Increment',
  ERROR = '[Main] Error',
  RETRIEVE = '[Main] Retrieve',
  GETMARKET = '[Main] Get market',
  SETMARKET = '[Main] Set market',
  SETMLMARKET = '[Main] Set ml market',
  GETMARKETS = '[Main] Get markets',
  SETMARKETS = '[Main] Set markets',
  GETCONFIG = '[Main] Get config',
  SETCONFIG = '[Main] Set config',
  SETCONFIGVALUE = '[Main] Set config value',
  SETCONFIGVALUEMAP = '[Main] Set config value map',
  SETSTARTDATE = '[Main] Set start date',
  SETENDDATE = '[Main] Set end date',
  GETEVOLVE = '[Main] Get evolve',
  GETCONTENT = '[Main] Get content',
  GETCONTENTEVOLVE = '[Main] Get content evolve',
  GETCONTENTDATASET = '[Main] Get content dataset',
  GETCONTENTCROSSTEST = '[Main] Get content crosstest',
  GETCONTENTFILTER = '[Main] Get content filter',
  GETCONTENTABOVEBELOW = '[Main] Get content abovebelow',
  GETCONTENTMACHINELEARNING = '[Main] Get content machine learning',
  GETCONTENTIMPROVE = '[Main] Get content improve',
  GETSINGLEMARKET = '[Main] Get single market',
  GETSINGLEMARKETLOOP = '[Main] Get single market loop',
  GETVERIFY = '[Main] Get verify',
  GETVERIFYLOOP = '[Main] Get verify loop',
  GETIMPROVEPROFIT = '[Main] Get improve profit',
  GETIMPROVEABOVEBELOW = '[Main] Get improve above below',
  NEWTAB = '[Main] New tab',
  CHANGE_ANIMATIONS_PAGE = '[Main] Change Animations Page',
  CHANGE_ANIMATIONS_PAGE_DISABLED = '[Main] Change Animations Page Disabled',
  CHANGE_ANIMATIONS_ELEMENTS = '[Main] Change Animations Elements',
  PERSIST = '[Main] Persist'
}

export class ActionMainChangeLanguage implements Action {
  readonly type = MainActionTypes.CHANGE_LANGUAGE;

  constructor(readonly payload: { language: Language }) {}
}

export class ActionMainChangeTheme implements Action {
  readonly type = MainActionTypes.CHANGE_THEME;

  constructor(readonly payload: { theme: string }) {}
}

export class ActionMainChangeAutoNightMode implements Action {
  readonly type = MainActionTypes.CHANGE_AUTO_NIGHT_AUTO_MODE;

  constructor(readonly payload: { autoNightMode: boolean }) {}
}

export class ActionMainChangeStickyHeader implements Action {
  readonly type = MainActionTypes.CHANGE_STICKY_HEADER;

  constructor(readonly payload: { stickyHeader: boolean }) {}
}

export class ActionMainChangeAnimationsPage implements Action {
  readonly type = MainActionTypes.CHANGE_ANIMATIONS_PAGE;

  constructor(readonly payload: { pageAnimations: boolean }) {}
}

export class ActionMainChangeAnimationsPageDisabled implements Action {
  readonly type = MainActionTypes.CHANGE_ANIMATIONS_PAGE_DISABLED;

  constructor(readonly payload: { pageAnimationsDisabled: boolean }) {}
}

export class ActionMainChangeAnimationsElements implements Action {
  readonly type = MainActionTypes.CHANGE_ANIMATIONS_ELEMENTS;

  constructor(readonly payload: { elementsAnimations: boolean }) {}
}

export class ActionMainPersist implements Action {
  readonly type = MainActionTypes.PERSIST;

  constructor(readonly payload: { main: MainState }) {}
}

export class ActionIncrement implements Action {
  readonly type = MainActionTypes.INCREMENT;
  //console.log("here");

  constructor(readonly payload: { incCount: number }) {}
}

export class ActionGetmarket implements Action {
  readonly type = MainActionTypes.GETMARKET;

  constructor(readonly payload: { incCount: number }) {}
}

export class ActionGetmarkets implements Action {
  readonly type = MainActionTypes.GETMARKETS;

  constructor() {}
}

export class ActionSetmarkets implements Action {
  readonly type = MainActionTypes.SETMARKETS;
  //console.log("here");
  //constructor() {}
  //constructor(readonly payload: { res: any }) {}
  constructor(readonly payload: { markets: Array<string> }) {}
}

export class ActionSetmarket implements Action {
  readonly type = MainActionTypes.SETMARKET;
  //console.log("here");
  //constructor() {}
  //constructor(readonly payload: { res: any }) {}
  constructor(readonly payload: { market: string }) {}
}

export class ActionSetmlmarket implements Action {
  readonly type = MainActionTypes.SETMLMARKET;
  //console.log("here");
  //constructor() {}
  //constructor(readonly payload: { res: any }) {}
  constructor(readonly payload: { mlmarket: string }) {}
}

export class ActionSetconfig implements Action {
  readonly type = MainActionTypes.SETCONFIG;

  constructor(readonly payload: { config: any }) {}
}

export class ActionSetconfigvalue implements Action {
  readonly type = MainActionTypes.SETCONFIGVALUE;

  constructor(readonly payload: any) {}
}

export class ActionSetconfigvaluemap implements Action {
  readonly type = MainActionTypes.SETCONFIGVALUEMAP;

  constructor(readonly payload: Array<any>) {}
}

export class ActionGetconfig implements Action {
  readonly type = MainActionTypes.GETCONFIG;

  constructor() {}
}

export class ActionRetrieve implements Action {
  readonly type = MainActionTypes.RETRIEVE;

  constructor(readonly payload: { url: string }) {}
}

export class ActionError implements Action {
  readonly type = MainActionTypes.ERROR;

  constructor(readonly payload: { error: HttpErrorResponse }) {}
}

export class ActionSetstartdate implements Action {
  readonly type = MainActionTypes.SETSTARTDATE;

  constructor(readonly payload: { startdate: string }) {}
}

export class ActionSetenddate implements Action {
  readonly type = MainActionTypes.SETENDDATE;

  constructor(readonly payload: { enddate: string }) {}
}

export class ActionGetevolve implements Action {
  readonly type = MainActionTypes.GETEVOLVE;

  constructor(readonly payload: any) {}
}

export class ActionGetcontent implements Action {
  readonly type = MainActionTypes.GETCONTENT;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentEvolve implements Action {
  readonly type = MainActionTypes.GETCONTENTEVOLVE;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentDataset implements Action {
  readonly type = MainActionTypes.GETCONTENTDATASET;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentCrosstest implements Action {
  readonly type = MainActionTypes.GETCONTENTCROSSTEST;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentFilter implements Action {
  readonly type = MainActionTypes.GETCONTENTFILTER;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentAboveBelow implements Action {
  readonly type = MainActionTypes.GETCONTENTABOVEBELOW;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentMachineLearning implements Action {
  readonly type = MainActionTypes.GETCONTENTMACHINELEARNING;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetcontentImprove implements Action {
  readonly type = MainActionTypes.GETCONTENTIMPROVE;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetImproveAboveBelow implements Action {
  readonly type = MainActionTypes.GETIMPROVEABOVEBELOW;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetSingleMarket implements Action {
  readonly type = MainActionTypes.GETSINGLEMARKET;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetSingleMarketLoop implements Action {
  readonly type = MainActionTypes.GETSINGLEMARKETLOOP;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetVerify implements Action {
  readonly type = MainActionTypes.GETVERIFY;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetVerifyLoop implements Action {
  readonly type = MainActionTypes.GETVERIFYLOOP;

  constructor(readonly payload: { config: any }) {}
}

export class ActionGetImproveProfit implements Action {
  readonly type = MainActionTypes.GETIMPROVEPROFIT;

  constructor(readonly payload: { config: any }) {}
}

export class ActionNewtab implements Action {
  readonly type = MainActionTypes.NEWTAB;

  constructor(readonly payload: { tab: any }) {}
}

export type MainActions =
  | ActionMainPersist
  | ActionMainChangeLanguage
  | ActionMainChangeTheme
  | ActionMainChangeAnimationsPage
  | ActionMainChangeAnimationsPageDisabled
  | ActionMainChangeAnimationsElements
  | ActionMainChangeAutoNightMode
  | ActionIncrement
  | ActionGetmarket
  | ActionGetmarkets
  | ActionSetmarket
  | ActionSetmarkets
  | ActionGetconfig
  | ActionSetconfig
  | ActionSetconfigvalue
  | ActionSetconfigvaluemap
  | ActionSetstartdate
  | ActionSetenddate
  | ActionRetrieve
  | ActionError
  | ActionGetevolve
  | ActionGetcontent
  | ActionGetcontentEvolve
  | ActionGetcontentDataset
  | ActionGetcontentCrosstest
  | ActionGetcontentFilter
  | ActionGetcontentAboveBelow
  | ActionGetcontentMachineLearning
  | ActionGetcontentImprove
  | ActionGetSingleMarket
  | ActionGetSingleMarketLoop
  | ActionGetVerify
  | ActionGetVerifyLoop
  | ActionGetImproveProfit
  | ActionGetImproveAboveBelow
  | ActionNewtab
  | ActionMainChangeStickyHeader;
