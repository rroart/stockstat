import { Store, select } from '@ngrx/store';
import { Component, OnDestroy, OnInit, Input } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable, Subject } from 'rxjs';
import { filter, takeUntil, map } from 'rxjs/operators';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatInputModule } from '@angular/material/input';

import { ActionIncrement, ActionSetconfigvaluemap } from '../main.actions';

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
  selector: 'iclijtreeview',
  templateUrl: './iclijtreeview.component.html',
  //styleUrls: ['./main.component.scss'],
  animations: [routeAnimations]
})
export class IclijTreeviewComponent implements OnInit, OnDestroy {
  private unsubscribe$: Subject<void> = new Subject<void>();
  private isAuthenticated$: Observable<boolean>;

  main: MainState;

@Input() configMaps : any;
@Input() elem : any; //ConfigTreeMap;
@Input() configValueMap : Map<String, String>;
values2: Array<any>;
    textname: String;
myvalue: String;
checkbox: boolean = false;
checkboxvalue: any; //boolean;


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
            this.values2 = Object.values(this.elem["configTreeMap"]);
        this.textname = this.configMaps.text[this.elem.name];
        if (this.textname == null) {
           this.textname = this.elem.name;
           let idx = this.textname.indexOf(".");
           if (idx >= 0) {
              this.textname = this.textname.substring(idx + 1);
           }
        }

	//console.log(this.configValueMap);
    this.myvalue = this.configValueMap[this.elem.name];

        const typename = this.configMaps.map[this.elem.name];
        this.myvalue = this.configValueMap[this.elem.name];
        if (typename == "java.lang.Boolean") {
           this.checkbox = true;
	   //console.log(this.myvalue);
	   this.checkboxvalue = this.myvalue;
	   if (this.checkboxvalue == null) {
           //this.checkboxvalue = false;
	   }
         //console.log("boolval " + this.checkboxvalue);
           }

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

        switchMe(event) {
        console.log(event.checked);
        console.log("switched2"+event.checked);
    console.log("va3 " + this.elem.name + " " + event.checked);
    //this.configValueMap.set(this.elem.name, event.target.checked);
    // TODO same
    this.store.dispatch(new ActionSetconfigvaluemap([this.elem.name, event.checked]));
        }

    onSubmit($event) {
    console.log($event);
    console.log("va " + this.elem.name + " " +$event.target.value);
    //this.configValueMap.set(this.elem.name, value);
    // TODO same
    this.store.dispatch(new ActionSetconfigvaluemap([this.elem.name, $event.target.value]));
}

async delay(ms: number) {
    await new Promise(resolve => setTimeout(()=>resolve(), ms)).then(()=>console.log("fired"));
}
}
