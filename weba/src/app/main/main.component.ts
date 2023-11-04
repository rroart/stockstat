import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';

import { ActionIncrement, ActionGetmarkets, ActionGetconfig, ActionGetconfig2 } from './main.actions';

import { routeAnimations, TitleService } from '@app/core';
//import { tick } from '@angular/core';
import {
  State as BaseSettingsState,
  selectSettings,
  SettingsState
} from '@app/settings';

import { selectMain, selectTabs } from './main.selectors';
import { MainState } from './main.state';
import { State as BaseMainState } from './main.state';
import { selectAuth } from '@app/core/auth/auth.selectors';

import { Client, ConvertToSelect } from './Client';

import { MytableComponent } from './table/mytable.component';
import { MyIclijtableComponent } from './iclijtable/myiclijtable.component';
import { MyimageComponent } from './table/myimage.component';

interface State extends BaseSettingsState, BaseMainState {}

@Component({
  selector: 'anms-main',
  templateUrl: './main.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class MainComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  count: number;
  main: MainState;

  maintabs = [
    { link: 'todos', label: 'anms.main.menu.todos' },
    { link: 'stock-market', label: 'anms.main.menu.stocks' },
    { link: 'theming', label: 'anms.main.menu.theming' },
    { link: 'crud', label: 'anms.main.menu.crud' },
    { link: 'form', label: 'anms.main.menu.form' },
    { link: 'notifications', label: 'anms.main.menu.notifications' },
    { link: 'authenticated', label: 'anms.main.menu.auth', auth: true }
  ];

  mytabs: any;
  mystuff: any;
  
  constructor(
    private store: Store<State>,
    private router: Router,
    private titleService: TitleService,
    private translate: TranslateService
  ) {}

  ngOnInit(): void {
    this.store.dispatch(new ActionGetconfig());
    this.store.dispatch(new ActionGetmarkets());
    this.store.dispatch(new ActionGetconfig2());
    this.translate.setDefaultLang('en');
    this.subscribeToMain();
    this.subscribeToTabs();
    this.subscribeToSettings();
    this.subscribeToRouterEvents();
    this.isAuthenticated$ = this.store.pipe(
      select(selectAuth),
      map(auth => auth.isAuthenticated)
    );
    this.mytabs = this.main.tabs;

    const myrows = [];
    const obj1 = new Object();
    obj1['cols']=['a','b','c'];
    const obj2 = new Object();
    obj2['cols']=[1,2,3];
    const obj3 = new Object();
    obj3['cols']=[4,5,6];
    const obj4 = new Object();
    obj4['cols']=[7,8,9];
    myrows.push(obj1);
    myrows.push(obj2);
    myrows.push(obj3);
    myrows.push(obj4);
    const obj = new Object();
    obj['rows']=myrows;
    obj['blbl']="bla";
    const myarray = [];
    myarray.push(obj);
    this.testarray = myarray;
    console.log(myarray);
  }

  testarray: any;

  ngOnDestroy(): void {
    this.unsubscribe$.next();
    this.unsubscribe$.complete();
  }

  private subscribeToSettings() {
    this.store
      .pipe(
        select(selectSettings),
        takeUntil(this.unsubscribe$)
      )
      .subscribe((settings: SettingsState) =>
        this.translate.use(settings.language)
      );
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
        this.setCount(main);
      });
  }

  private subscribeToTabs() {
    this.store
      .pipe(
        select(selectTabs),
        takeUntil(this.unsubscribe$)
      )
      .subscribe((main: MainState) => {
      this.mytabs = main;
      console.log(main);
      });
  }

  private setCount(main: MainState) {
  console.log(main);
    this.count = main.count;
  }

  //@ViewChild('increment') increment;

}
