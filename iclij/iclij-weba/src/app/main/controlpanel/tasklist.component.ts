import { Store, select } from '@ngrx/store';
import { Component, OnInit, AfterViewInit, OnDestroy } from '@angular/core';
import {interval} from "rxjs/internal/observable/interval";
import { Subscription } from 'rxjs';
import {startWith, switchMap} from "rxjs/operators";
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import {
  State as BaseSettingsState,
  selectSettings,
  SettingsState
} from '@app/settings';
import { selectMain } from '../main.selectors';
import { MainState } from '../main.state';
import { State as BaseMainState } from '../main.state';
import { AppService } from './app.service'
import {
  ActionGettasks,
} from '../main.actions';

interface State extends BaseSettingsState, BaseMainState {}

@Component({
  selector: 'tasklist',
  templateUrl: './tasklist.component.html',
  //styleUrls: ['./app.component.sass']
})
export class TaskListComponent implements OnInit,OnDestroy{
  private unsubscribe$: Subject<void> = new Subject<void>();
  timeInterval: Subscription;
  title = 'Polling in angular';
  status:any;
  values2: Array<string>;

  main: MainState;

  constructor(
    private store: Store<State>
  ) {}
  
  ngOnInit(): void {
    this.subscribeToMain();
    this.store.dispatch(new ActionGettasks());
  }
;
  ngOnDestroy(): void {
    //this.timeInterval.unsubscribe();
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

}