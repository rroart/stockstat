import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, OnChanges, SimpleChanges, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';

import { ActionIncrement } from '../main.actions';

import { routeAnimations, TitleService } from '@app/core';
//import { tick } from '@angular/core';
import {
  State as BaseSettingsState,
  selectSettings,
  SettingsState
} from '@app/settings';

import { selectMain, selectConfig } from '../main.selectors';
import { MainState } from '../main.state';
import { State as BaseMainState } from '../main.state';
import { selectAuth } from '@app/core/auth/auth.selectors';

interface State extends BaseSettingsState, BaseMainState {}

@Component({
  selector: 'configtree',
  templateUrl: './configtree.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class ConfigtreeComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  @Input()
  config: any;

  @Input()
  main: MainState;

  tmpmain: MainState;
  realconfig: any;
  
  maps: any;
  values2: any;
  configValueMap: Map<String, Object>;
  
  configMaps:any;

  constructor(
    private store: Store<State>,
    private router: Router,
    private titleService: TitleService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.translate.setDefaultLang('en');
    this.subscribeToMain();
    this.subscribeToConfig();
    //this.subscribeToSettings();
    this.subscribeToRouterEvents();
    this.isAuthenticated$ = this.store.pipe(
      select(selectAuth),
      map(auth => auth.isAuthenticated)
    );

    const config = this.main.config;
    console.log(this.tmpmain);
    console.log(this.main);
    console.log(config);
    //var configTreeMap = config && config.get('configTreeMap') ? config.get('configTreeMap') : new Map();
    var configTreeMap = config && config.configTreeMap ? config.configTreeMap : new Map();
    
    if (configTreeMap === null) {
      configTreeMap = new Map();
    }
    //console.log(configTreeMap);
    //const confMap = configTreeMap.has('configTreeMap') ? configTreeMap.get('configTreeMap') : [];
    const confMap = configTreeMap.configTreeMap ? configTreeMap.configTreeMap : [];
    const now = Date.now();
    //const map2 = confMap.map((i, j) => this.getview(i, j, now));
    //this.maps = Array.from(map2.values());
    console.log(this.main);
    console.log(this.realconfig);
    if (this.realconfig != null) {
   this.configMaps = this.realconfig.configMaps;
  this.configValueMap = this.realconfig.configValueMap;
    this.values2 = Object.values(this.realconfig.configTreeMap.configTreeMap);
       console.log(this.realconfig.configValueMap);
       console.log(this.configValueMap["predictors[@enable]"]);
    console.log(this.configValueMap["predictors.lstm.horizon"]);
    }
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

ngOnChanges(changes: SimpleChanges) {
     for (let propName in changes) {  
 let change = changes[propName];
 let curVal  = JSON.stringify(change.currentValue);
 let prevVal = JSON.stringify(change.previousValue);

        //console.log(curVal);
        //console.log(prevVal);
     }  
    console.log(this.main);
    console.log(this.realconfig);
    if (this.realconfig != null) {
   this.configMaps = this.realconfig.configMaps;
    this.configValueMap = this.realconfig.configValueMap;
    this.values2 = Object.values(this.realconfig.configTreeMap.configTreeMap);
       console.log(this.configValueMap["predictors[@enable]"]);
    console.log(this.configValueMap["predictors.lstm.horizon"]);
}
    console.log("end");
}

  private subscribeToRouterEvents() {
    this.titleService.setTitle(
      this.router.routerState.snapshot.root,
      this.translate
    );
    this.router.events
      .pipe(
        filter(event => event instanceof ActivationEnd),
        map((event: ActivationEnd) => event.snapshot),
        takeUntil(this.unsubscribe$)
      )
      .subscribe(snapshot =>
        this.titleService.setTitle(snapshot, this.translate)
      );
  }

  private subscribeToConfig() {
    this.store
      .pipe(
        select(selectConfig),
        takeUntil(this.unsubscribe$)
      )
      .subscribe((main: MainState) => {
      this.realconfig = main;
      console.log(main);
      });
      /*
      (main: MainState) => {
        this.tmpmain = main;
        //this.main = main;
        //this.setCount(main);
      });
      */
  }

  private subscribeToMain() {
    this.store
      .pipe(
        select(selectMain),
        takeUntil(this.unsubscribe$)
      )
      .subscribe((main: MainState) => {
        this.tmpmain = main;
        //this.main = main;
        //this.setCount(main);
      });
  }

  private setCount(main: MainState) {
  console.log(main);
    //this.count = main.count;
  }

  //@ViewChild('increment') increment;

  increment($event) {
  console.log('incremnent');
  console.log($event);
  console.log(this);
  this.store.dispatch(new ActionIncrement({incCount: 2}));
  //this.increment.focus();
  }
}
