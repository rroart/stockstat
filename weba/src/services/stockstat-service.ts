import { Injectable } from '@angular/core';
import {Http, Response, Headers, RequestOptions} from '@angular/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/forkJoin';
import 'rxjs/add/operator/toPromise';
import './config';
import { ServiceResult } from './serviceresult';
import { ServiceParam } from './serviceresult';
import { MyConfig } from "./config";
import { ConfigTreeMap } from "./configtreemap";

@Injectable()
export class StockstatService {
    config: MyConfig;
    markets: Array<String> = [];
    backendUrl = 'http://localhost:12345';

    constructor(public http: Http) {
    }

    getMarkets(){
        let headers = new Headers({ 'Content-Type': 'application/json' });
	let options = new RequestOptions({ headers: headers });	
    
        let param = { market: "cboevol" };
        return this.http.post(this.backendUrl + '/getmarkets', param)
//	    .toPromise()
//	    .then(response => { return <ServiceResult>response.json() }, this.handleError);
           .map((res: Response) => <ServiceResult>res.json())
           .catch(StockstatService.handleError);
//                         .catch((error:any) => Observable.throw(error.json().error || 'Server error')) //...errors if
//			 .subscribe();
    }

    getMarkets3(){
        return this.http.post(this.backendUrl + '/getmarkets', "")
            .map((res: Response) => <String[]>res.json())
            .catch(StockstatService.handleError);
    }

    getConfig(){
	console.log("getconfig");
        let headers = new Headers({ 'Content-Type': 'application/json' });
	let options = new RequestOptions({ headers: headers });	
        let param = { market: "cboevol" };
        return this.http.post(this.backendUrl + '/getconfig', param)
//            .map((res: Response) => <ServiceResult>res.json())
//            .map((res: Response) => new ServiceResult().deserialize(res.json()))
            .map((res: Response) => {
	    let res2 = <ServiceResult> res.json();
	    Object.setPrototypeOf(res2, ServiceResult.prototype);
	    console.log("here1111");
	    console.log(res2)
	    return res2;
	    })
//	    .subscribe((res:ServiceResult) => this.postResponse = res
            .catch(StockstatService.handleError);
	//return this.http.post(this.backendUrl + '/getconfig', param)
	//       .map((res: Response) => res.json())
        //       .subscribe((json: Object) => {
        //    let sr: ServiceResult = new ServiceResult();
	//    Object.assign(sr, json);
	//    console.log(typeof sr);
	//    console.log(sr);
        //});
    }

    getConfigSub(){
	console.log("getconfigsub");
        let headers = new Headers({ 'Content-Type': 'application/json' });
	let options = new RequestOptions({ headers: headers });	
        let param = { market: "cboevol" };
        return this.http.post(this.backendUrl + '/getconfig', param)
//            .map((res: Response) => <ServiceResult>res.json())
//            .map((res: Response) => new ServiceResult().deserialize(res.json()))
//            .map((res: Response) => res.json())
//	    .subscribe((res:ServiceResult) => this.postResponse = res
//	    .map((res: Response) => deserialize(ServiceResult, res.json().data))
	    .map((res: Response) => res.json())
//	    .subscribe((res:ServiceResult) => this.postResponse = res
            .catch(StockstatService.handleError);
	//return this.http.post(this.backendUrl + '/getconfig', param)
	//       .map((res: Response) => res.json())
        //       .subscribe((json: Object) => {
        //    let sr: ServiceResult = new ServiceResult();
	//    Object.assign(sr, json);
	//    console.log(typeof sr);
	//    console.log(sr);
        //});
    }

    setConfig(config: ServiceParam){
    		      console.log("setconfig");
    		      //console.log("serconf"+JSON.stringify(config.serialize(config)));
		      console.log(JSON.stringify(config));
		      //console.log(JSON.parse(JSON.stringify(config)));
		      //console.log(config.serialize());
        return this.http.post(this.backendUrl + '/setconfig', config)
            .map((res: Response) => <ServiceResult>res.json())
            .catch(StockstatService.handleError)
	    .subscribe();
    }

    addMarket(market: String){
        if (market) {
            this.markets.push(market);
            return market;
        }
    }

    static handleError(error: Response) {
        console.log("Error " + error);
        return Observable.throw(error || 'Server error');
    }
}
