import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { MyConfig } from "../../../services/config";
import { ServiceResult, ServiceParam } from "../../../services/serviceresult";
import { StockstatService } from "../../../services/stockstat-service";
import { ConfigTreeMap } from "../../../services/configtreemap";

@Component({
    selector: 'market-component',
    templateUrl: '/src/components/builder/stockstat/stockstat.component.html',
})
export class MarketComponent implements OnInit{
    marketList:Observable<ServiceResult>;
    servres:Observable<ServiceResult>;
    //marketList:String[];
    marketList2:String[];
    configTree:Observable<ServiceResult>;
    configTree2:ConfigTreeMap;
    marketList3:ServiceResult;
    config:MyConfig;
    errorMessage: any;
    market: String;

    constructor(
        public router:Router,
        public stockstatService:StockstatService) { console.log("hi3");
		       this.market="nordhist";
}

    ngOnInit() {
    	       console.log("hi4");
	let sr: Observable<ServiceResult> = this.stockstatService.getMarkets();
	//let serviceresult: ServiceResult = this.stockstatService.getMarkets();
        //this.marketList = sr.subscribe(markets => console.log(markets));
	//var gutil = require('gulp-util');
	//gutil.log('Hello world!');
	console.log("helo");
        this.marketList = this.stockstatService.getMarkets();
	this.marketList
	   .subscribe(result => {
	   this.marketList2 =result.markets;
	   console.log("this0");
	   console.log(typeof this.marketList);
	   console.log(typeof result);
	   console.log(typeof this.marketList2);
	   console.log(this.marketList2);
	   console.log("this1");
	   }
	   );
	   console.log("gr0");
        this.configTree = this.stockstatService.getConfig();
//	this.configTree
//	   .subscribe(result => this.configTree2 =result.configTreeMap);
	this.marketList
	   .subscribe(result => this.config =result.config);
	this.marketList
	   .subscribe(result => this.marketList3 =result);
	
this.config = new MyConfig(null, null, null, null, null, null, null);
console.log("thismarket " + this.market);
				this.config.mymarket = this.market;
				let param = new ServiceParam(this.config, null, null);
//				this.stockstatService.setConfig(param);
    }

    onSelect(market:String) {
    			    console.log("market " + market);
    }

    getmarket() {
    //var gutil = require('gulp-util');
    //gutil.log('Hello world2!');
    				console.log("markethere " + this.market);
				console.log("conf " + this.stockstatService.config);
				console.log("conf " + this.stockstatService.config.constructor.name);
				this.stockstatService.config.mymarket = this.market;
				let param = new ServiceParam(this.stockstatService.config, null, null);
				this.stockstatService.setConfig(param);
    }

}
