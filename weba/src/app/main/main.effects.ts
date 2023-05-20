import { Injectable } from '@angular/core';
import { LocalStorageService } from '@app/core';
import { Actions, Effect, ofType } from '@ngrx/effects';
import { Action } from '@ngrx/store';
import { asyncScheduler, interval, of } from 'rxjs';
import { MytableComponent } from './table/mytable.component';

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
  ActionGetconfig2,
  ActionSetconfig,
  ActionSetconfig2,
  ActionSetconfigvaluemap,
  ActionGettasks,
  ActionSettasks,
  ActionRetrieve,
  ActionError,
  ActionGetcontent,
  ActionGetcontentGraph,
  ActionGetcontent2,
  ActionGetcontentEvolve,
  ActionGetcontentDataset,
  ActionGetcontentCrosstest,
  ActionGetcontentFilter,
  ActionGetcontentAboveBelow,
  ActionGetcontentMachineLearning,
  ActionGetcontentImprove,
  ActionGetImproveAboveBelow,
  ActionGetSingleMarket,
  ActionGetSingleMarketLoop,
  ActionGetVerify,
  ActionGetVerifyLoop,
  ActionGetImproveProfit,
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
          map(res => new ActionSetconfig({ config: res['configData'] })),
          catchError(error => of(new ActionError({ error })))
        )
	//console.log(res);
	//return new ActionSetmarkets({ markets: res['markets']});
	//return res['markets'];
	}
      )
    );

  @Effect()
  getconfiguration2 = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetconfig>(MainActionTypes.GETCONFIG2),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetconfig2) => {
        console.log(action);
        const res = this.service.retrieve3('/getconfig', {});
	// action.payload.url
	//console.log(res);
	//const res3 = res.pipe(map(res => { console.log(res); return res; } ));
	//const res2 = res.pipe(map(res => { return res.json(); } ));
	//console.log(res2);
        return this.service.retrieve3('/getconfig', {}).pipe(
          map(res => new ActionSetconfig2({ config2: res['configData'] })),
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
	param['configData'] = getMyConfig(config, param['market'], date);
	var neuralnetcommand = new Object();
	neuralnetcommand['mllearn'] = false;
	neuralnetcommand['mlclassify'] = true;
	neuralnetcommand['mldynamic'] = false;
	param['neuralnetcommand'] = neuralnetcommand;
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
  getcontentgraph = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentGraph>(MainActionTypes.GETCONTENTGRAPH),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentGraph) => {
        console.log(action);
	const config = action.payload.config;
	const date = config['enddate'];
	var param = new Object();
	param['market'] = config['market'];
	param['configData'] = getMyConfig(config, param['market'], date);
	const id = action.payload.value;
	console.log(id);
	const ids = [param['market'] + "," + id];
	param['ids'] = ids;
	console.log(ids);
	const guisize = new Object();
	guisize['x']=600;
    	guisize['y']=400;
        param['guiSize'] = guisize;
        return this.service.retrieve('/getcontentgraph2', param).pipe(
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
	serviceparam['configData'] = getMyConfig(config, serviceparam['market'], date);
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
  @Effect()
  getcontent2 = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontent>(MainActionTypes.GETCONTENT2),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontent) => {
        console.log(action);
        const config = action.payload; //.config;
        console.log(config);
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontent', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentevolve = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentEvolve>(MainActionTypes.GETCONTENTEVOLVE),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentEvolve) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentevolve', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentdataset = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentDataset>(MainActionTypes.GETCONTENTDATASET),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentDataset) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentdataset', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentcrosstest = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentCrosstest>(MainActionTypes.GETCONTENTCROSSTEST),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentCrosstest) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentcrosstest', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentfilter = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentFilter>(MainActionTypes.GETCONTENTFILTER),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentFilter) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentfilter', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentabovebelow = ({
    debounce = 500,
    scheduler = asyncScheduler
  } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentAboveBelow>(MainActionTypes.GETCONTENTABOVEBELOW),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentAboveBelow) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentabovebelow', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentmachinelearning = ({
    debounce = 500,
    scheduler = asyncScheduler
  } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentMachineLearning>(
        MainActionTypes.GETCONTENTMACHINELEARNING
      ),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentMachineLearning) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentmachinelearning', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getcontentimprove = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetcontentImprove>(MainActionTypes.GETCONTENTIMPROVE),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetcontentImprove) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getcontentimprove', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getsinglemarket = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetSingleMarket>(MainActionTypes.GETSINGLEMARKET),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetSingleMarket) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/findprofit', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getsinglemarketloop = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetSingleMarketLoop>(MainActionTypes.GETSINGLEMARKETLOOP),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetSingleMarketLoop) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['startdate'];
        console.log(date);
        var param = new Object();
        param['market'] = config['market'];
        const iclijConfig = getMyConfig(config, param['market'], date);
        param['configData'] = iclijConfig;
        const loops = iclijConfig['configValueMap']['singlemarket.loops'];
        var i;
        for (i = 0; i < loops; i++) {
          param['offset'] =
            i * iclijConfig['configValueMap']['singlemarket.loopinterval'];
          return this.service.retrieve2('/findprofit', param).pipe(
            map(res => new ActionNewtab(res.lists)),
            catchError(error => of(new ActionError({ error })))
          );
        }
      })
    );

  @Effect()
  getverify = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetVerify>(MainActionTypes.GETVERIFY),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetVerify) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getverify', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getverifyloop = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetVerifyLoop>(MainActionTypes.GETVERIFYLOOP),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetVerifyLoop) => {
        console.log(action);
        const config = action.payload; //.config;
        console.log(config);
        console.log(config['verification']);
        const date = config['startdate'];
        console.log(date);
        var param = new Object();
        param['market'] = config['market'];
        const iclijConfig = getMyConfig(config, param['market'], date);
        param['configData'] = iclijConfig;
        const loops = iclijConfig['configValueMap']['verification.loops'];
        var i;
        for (i = 0; i < loops; i++) {
          param['offset'] =
            i * config['configValueMap']['verification.loopinterval'];
          return this.service.retrieve2('/getverify', param).pipe(
            map(res => new ActionNewtab(res.lists)),
            catchError(error => of(new ActionError({ error })))
          );
        }
      })
    );

  @Effect()
  getimprove = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetImproveProfit>(MainActionTypes.GETIMPROVEPROFIT),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetImproveProfit) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getimprove', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getimproveabovebelow = ({
    debounce = 500,
    scheduler = asyncScheduler
  } = {}) =>
    this.actions$.pipe(
      ofType<ActionGetImproveAboveBelow>(MainActionTypes.GETIMPROVEABOVEBELOW),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGetImproveAboveBelow) => {
        console.log(action);
        const config = action.payload; //.config;
        const date = config['enddate'];
        var param = new Object();
        param['market'] = config['market'];
        param['configData'] = getMyConfig(config, param['market'], date);
        return this.service.retrieve2('/getimproveabovebelow', param).pipe(
          map(res => new ActionNewtab(res.lists)),
          catchError(error => of(new ActionError({ error })))
        );
      })
    );

  @Effect()
  getevolve2 = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
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
        serviceparam['configData'] = getMyConfig(
          config,
          serviceparam['market'],
          date
        );
        if (id != null) {
          console.log(id);
          const ids = [id];
          serviceparam['ids'] = ids;
        }
        //serviceparam.market = '0';
        console.log('hereevolve');
        console.log(webpath);
        //console.log(JSON.stringify(serviceparam));
        return this.service.retrieve2('/' + webpath, serviceparam).pipe(
          map(result => {
            console.log(result);
            if (set) {
              const update = result.maps.update;
              for (const [key, value] of Object.entries(update)) {
                //console.log(key);
                new ActionSetconfigvaluemap([key, value]);
              }
            }
            const list = result.list;
            console.log('here');
            return new ActionNewtab(result.list);
            console.log('here');
          }),
          catchError(error => of(new ActionError({ error })))
        );
        //console.log(res);
        //return new ActionSetmarkets({ markets: res['markets']});
        //return res['markets'];
      })
    );
    
  @Effect()
  gettasks = ({ debounce = 500, scheduler = asyncScheduler } = {}) =>
    this.actions$.pipe(
      ofType<ActionGettasks>(MainActionTypes.GETTASKS),
      debounceTime(debounce, scheduler),
      switchMap((action: ActionGettasks) => {
        console.log(action);
        //const res = this.service.retrieve0('/gettasks', {});
        // action.payload.url
        //console.log(res);
        //const res3 = res.pipe(map(res => { console.log(res); return res; } ));
        //const res2 = res.pipe(map(res => { return res.json(); } ));
        //console.log(res2);
	
        return this.service.retrieve3('/gettasks', {}).pipe(
          map(res => new ActionSettasks({ tasks: res })),
          catchError(error => of(new ActionError({ error })))
        );
        //console.log(res);
        //return new ActionSetmarkets({ markets: res['markets']});
        //return res['markets'];
      })
    );

  @Effect()
  gettasksinterval$ = interval(60000).pipe(
      switchMap(() => {
        //const res = this.service.retrieve0('/gettasks', {});
        // action.payload.url
        //console.log(res);
        //const res3 = res.pipe(map(res => { console.log(res); return res; } ));
        //const res2 = res.pipe(map(res => { return res.json(); } ));
        //console.log(res2);
	
        return this.service.retrieve3('/gettasks', {}).pipe(
          map(res => new ActionSettasks({ tasks: res })),
          catchError(error => of(new ActionError({ error })))
        );
        //console.log(res);
        //return new ActionSetmarkets({ markets: res['markets']});
        //return res['markets'];
      })
    );

}
