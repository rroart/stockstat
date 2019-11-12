import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';

import { ActionIncrement, ActionSetstartdate, ActionSetenddate, ActionGetcontent, ActionSetconfigvalue } from '../main.actions';

import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';

import { routeAnimations, TitleService } from '@app/core';
//import { tick } from '@angular/core';
import {
  State as BaseSettingsState,
  selectSettings,
  SettingsState
} from '@app/settings';

import { selectMain } from '../main.selectors';
import { MainState } from '../main.state';
import { State as BaseMainState } from '../main.state';
import { selectAuth } from '@app/core/auth/auth.selectors';
import { ActionGetmarkets, ActionSetmarket } from '../main.actions';

interface State extends BaseSettingsState, BaseMainState {}

@Component({
  selector: 'marketbar',
  templateUrl: './marketbar.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class MarketbarComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  main: MainState;

  markets: Array<string>;

  constructor(
    private store: Store<State>,
    private router: Router,
    private titleService: TitleService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.translate.setDefaultLang('en');
    this.subscribeToMain();
    //this.subscribeToSettings();
    this.subscribeToRouterEvents();
    this.isAuthenticated$ = this.store.pipe(
      select(selectAuth),
      map(auth => auth.isAuthenticated)
    );
    this.store.dispatch(new ActionGetmarkets());
    const value = new Date().toISOString();
    this.store.dispatch(new ActionSetenddate({ enddate: value}));
  }

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
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

  private subscribeToMain() {
    this.store
      .pipe(
        select(selectMain),
        takeUntil(this.unsubscribe$)
      )
      .subscribe((main: MainState) => {
        this.main = main;
        this.setMarkets(main);
      });
  }

  private setMarkets(main: MainState) {
  console.log(main);
    this.markets = main.markets;
  }

  //@ViewChild('increment') increment;

  changemarket($event) {
  console.log('market');
  console.log($event);
  console.log(this);
  this.store.dispatch(new ActionSetmarket({ 'market': $event.value }));
  this.store.dispatch(new ActionSetconfigvalue([ 'market', $event.value ]));
  //this.increment.focus();
  }
  incrementAsync($event) {
  console.log('incremnentas');
  console.log($event);
  //this.increment.focus();
  //tick(1000);
  this.delay(10000).then( () =>
  //setTimeout( () => { this.router.navigate(['/']); }, 5000);
  this.store.dispatch(new ActionIncrement({incCount: 2})));
  }

    handleStartDateChange($event) {
    console.log($event);
    this.store.dispatch(new ActionSetstartdate({ startdate: $event.value}));
  }

    handleEndDateChange($event) {
    console.log($event);
    this.store.dispatch(new ActionSetenddate({ enddate: $event.value}));
  }

    resetStartDate($event) {
    console.log($event);
    this.store.dispatch(new ActionSetstartdate({ startdate: null }));
  }

    resetEndDate($event) {
    console.log($event);
    this.store.dispatch(new ActionSetenddate({ enddate: null }));
  }

    getMarketData($event) {
    console.log($event);
    this.store.dispatch(new ActionGetcontent(this.main.config));
  }

async delay(ms: number) {
    await new Promise(resolve => setTimeout(()=>resolve(), ms)).then(()=>console.log("fired"));
}
}