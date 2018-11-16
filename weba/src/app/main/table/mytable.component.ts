import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, Input, ViewChild } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material';
import { MatTableModule, MatTableDataSource, MatSort } from '@angular/material';

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
  selector: 'mytable',
  templateUrl: './mytable.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class MytableComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  @Input()
  value: any;
  
  dataSource: any;
  displayedColumns: any;
  
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
    
    const array = this.value;
    //console.log(myarray);
    console.log(this.value);
    const array2 = array[0].rows;
    console.log(array2);
    const head = array2[0];
    const rest = array2.slice(1);
    console.log("start");
    console.log(head);
    console.log(rest);
    this.displayedColumns = head.cols;
    console.log(head.cols);
    console.log(rest);
    const result = [];
        for(var j = 0; j < rest.length; j++) {
        const row = rest[j];
        //console.log(row);
        //console.log(row.cols[0]);
        const newrow = [];
        for(var i = 0; i < head.cols.length; i++) {
            newrow[head.cols[i]] = row.cols[i];
        }
        result.push(newrow);
    }
    console.log(result);
    console.log("end");
    //this.dataSource = new MatTableDataSource(rest.cols);
    this.dataSource = new MatTableDataSource(result);
    console.log(this.sort);
    this.dataSource.sort = this.sort;
    console.log(this.dataSource);
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

@ViewChild(MatSort) sort: MatSort;

async delay(ms: number) {
    await new Promise(resolve => setTimeout(()=>resolve(), ms)).then(()=>console.log("fired"));
}
}
