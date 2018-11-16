import { Injectable } from '@angular/core';
import { LocalStorageService } from '@app/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { asyncScheduler, of } from 'rxjs';

import {
  catchError,
  debounceTime,
  map,
  switchMap,
  tap,
  distinctUntilChanged
} from 'rxjs/operators';
import {
  ActionIncrement,
  ActionGetmarket,
  ActionGetmarkets,
  ActionSetmarkets,
  ActionGetevolve,
  ActionGetconfig,
  ActionSetconfig,
  ActionSetconfigvaluemap,
  ActionRetrieve,
  ActionError,
  ActionGetcontent,
  ActionNewtab,
  MainActionTypes
} from './main.actions';
import { MainService } from './main.service';

function getMyConfig(config, market, date) {
    const myconfig = new Object();
    myconfig['configTreeMap'] = config['configTreeMap'];
    myconfig['configValueMap'] = config['configValueMap'];
    myconfig['text'] = config['text'];
    myconfig['deflt'] = config['deflt'];
    myconfig['type'] = config['type'];
    myconfig['date'] = date;
    myconfig['market'] = market;
    return myconfig;
}

@Injectable()
export class MainEffects {
  constructor(
    private actions$: Actions<Action>,
    private localStorageService: LocalStorageService,
    private service: MainService
  ) {}

  @Effect()
  getmarkets = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetmarkets>(MainActionTypes.GETMARKETS),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetmarkets) => {
        console.log(action);
        const res = this.service.retrieve('/getmarkets', {});
	// action.payload.url
	//console.log(res);
	//const res3 = res.pipe(map(res => { console.log(res); return res; } ));
	//const res2 = res.pipe(map(res => { return res.json(); } ));
	//console.log(res2);
        return this.service.retrieve('/getmarkets', {}).pipe(
          map(res => new ActionSetmarkets({ markets: res['markets'] })),
          catchError(error => of(new ActionError({ error })))
        )
	//console.log(res);
	//return new ActionSetmarkets({ markets: res['markets']});
	//return res['markets'];
	}
      )
    );

  @Effect()
  getconfiguration = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetconfig>(MainActionTypes.GETCONFIG),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetconfig) => {
        console.log(action);
        const res = this.service.retrieve('/getconfig', {});
	// action.payload.url
	//console.log(res);
	//const res3 = res.pipe(map(res => { console.log(res); return res; } ));
	//const res2 = res.pipe(map(res => { return res.json(); } ));
	//console.log(res2);
        return this.service.retrieve('/getconfig', {}).pipe(
          map(res => new ActionSetconfig({ config: res['config'] })),
          catchError(error => of(new ActionError({ error })))
        )
	//console.log(res);
	//return new ActionSetmarkets({ markets: res['markets']});
	//return res['markets'];
	}
      )
    );

  @Effect()
  getcontent = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontent>(MainActionTypes.GETCONTENT),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontent) => {
        console.log(action);
	const config = action.payload; //.config;
	const date = config['enddate'];
	var param = new Object();
	param['market'] = config['market'];
	param['config'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve('/getcontent', param).pipe(
          map(res => new ActionNewtab(res.list)),
          catchError(error => of(new ActionError({ error })))
        )
	//console.log(res);
	//return new ActionSetmarkets({ markets: res['markets']});
	//return res['markets'];
	}
      )
    );

  @Effect()
  getevolve = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetevolve>(MainActionTypes.GETEVOLVE),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetevolve) => {
        console.log(action);
        const array = action.payload;
        const webpath = array[0];
        const set = array[1];
    	const config = array[2];
	const id = array[3];
    	const serviceparam = new Object();
	// todo date is not here
    	const date = config['enddate'];
    	serviceparam['market'] = config['market'];
    	console.log(serviceparam['market']);
	console.log(date);
	serviceparam['config'] = getMyConfig(config, serviceparam['market'], date);
	if (id != null) {
	   console.log(id);
           const ids = [id];
           serviceparam['ids'] = ids;
	}
    	//serviceparam.market = '0';
	console.log("hereevolve");
        console.log(webpath);
	//console.log(JSON.stringify(serviceparam));
        return this.service.retrieve('/' + webpath, serviceparam).pipe(
          map(result => {
	  	     console.log(result);
	      if (set) {
	      	 const update = result.maps.update;
		 for (const [key, value] of Object.entries(update)) {
		     //console.log(key);
           	     new ActionSetconfigvaluemap([ key, value ]);
                 }
              }
    	      const list = result.list;
	      console.log("here");
    	      return new ActionNewtab(result.list);
	      console.log("here");
	  }),
          catchError(error => of(new ActionError({ error })))
        )
	//console.log(res);
	//return new ActionSetmarkets({ markets: res['markets']});
	//return res['markets'];
	}
      )
    );
}
