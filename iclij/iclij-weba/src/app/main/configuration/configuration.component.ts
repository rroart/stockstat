import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
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

import { selectMain } from '../main.selectors';
import { MainState } from '../main.state';
import { State as BaseMainState } from '../main.state';
import { selectAuth } from '@app/core/auth/auth.selectors';

interface State extends BaseSettingsState, BaseMainState {}

@Component({
  selector: 'configuration',
  templateUrl: './configuration.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class ConfigurationComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  main: MainState;

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
  incrementAsync($event) {
  console.log('incremnentas');
  console.log($event);
  //this.increment.focus();
  //tick(1000);
  this.delay(10000).then( () =>
  //setTimeout( () => { this.router.navigate(['/']); }, 5000);
  this.store.dispatch(new ActionIncrement({incCount: 2})));
  }

async delay(ms: number) {
    await new Promise(resolve => setTimeout(()=>resolve(), ms)).then(()=>console.log("fired"));
}
}
