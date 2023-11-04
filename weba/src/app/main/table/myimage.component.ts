import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';

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
  selector: 'myimage',
  templateUrl: './myimage.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class MyimageComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  @Input()
  value: any;
  
  dataSource: any;
  displayedColumns: any;
  data: any;
  url: any;
  
  main: MainState;

  @ViewChild('dataContainer', { static: true }) dataContainer: ElementRef;

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

    //this.data = '<svg version="1.1"     baseProfile="full"     width="300" height="200"     xmlns="http://www.w3.org/2000/svg">  <rect width="100%" height="100%" fill="red" />  <circle cx="150" cy="100" r="80" fill="green" />  <text x="150" y="125" font-size="60" text-anchor="middle" fill="white">SVG</text></svg>';
    //this.dataContainer.nativeElement.innerHTML = this.data;
    //return;
    var svgString = [
'<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="236" height="120" ',
'viewBox="0 0 236 120">',
'<rect x="14" y="23" width="200" height="50" fill="#55FF55" stroke="black" stroke-width="1" />',
'</svg>'];
    var svg64 = btoa(svgString.join(''));
    const stream = this.value;
    this.data = atob(stream.bytes);
    this.dataContainer.nativeElement.innerHTML = this.data;
    //this.url = 'data:image/svg+xml;base64,'+ this.data;
    //this.url = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==';
    //console.log(this.url.substring(0, 100));
    //this.data = svgString.join('');
    //console.log(this.data);
    //  <img [src]="url " width="1000" height="800"/>;

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
}
