import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { MarketComponent } from "../stockstat/stockstat.component";
import { ConfigTreeComponent } from "../configtree/configtree.component";
import { TreeViewComponent } from "../treeview/treeview.component";
import { MyConfig } from "../../../services/config";
import { ServiceResult, ServiceParam } from "../../../services/serviceresult";
import { StockstatService } from "../../../services/stockstat-service";
import {
  FormBuilder,
    FormGroup
    } from '@angular/forms';
    
@Component({
//    selector: 'singleintervaldays',
    selector: 'stockstat',
    templateUrl: '/src/components/builder/mainpage/mainpage.component.html',
})
export class MainPageComponent implements OnInit{
    configo:Observable<ServiceResult>;
    config:MyConfig;
//    singleintervaldays:number;
    res:ServiceResult;
    
    constructor(
        public router:Router,
        public stockstatService:StockstatService) {    				console.log("here6");
}

    ngOnInit() {
    				console.log("here5");
	let sr: Observable<ServiceResult> = this.stockstatService.getMarkets();
        this.configo = this.stockstatService.getConfig();
	//let serviceresult: ServiceResult = this.stockstatService.getMarkets();
        //this.marketList = sr.subscribe(markets => console.log(markets));
	this.configo
	.subscribe(result => this.res = result);
	console.log("h1");
	console.log(this.res);
	console.log("h1.5");
//	console.log(this.res.config);
//	console.log(this.singleintervaldays);
	console.log("h2");
//	this.configo
//	   .subscribe(result => this.config =result.config);
//	this.configo
//	   .subscribe(result => this.singleintervaldays =result.config.mytableintervaldays);
//	   this.singleintervaldays = this.config.mytableintervaldays;
	
    }

    onSubmit(market:String) {
    			    console.log("markethere2 " + market);
    //var gutil = require('gulp-util');
    //gutil.log('Hello world2!');
    				console.log(market)
				this.config.mymarket = market;
				let param = new ServiceParam(this.config, null, null);
//				this.stockstatService.setConfig(param);
    this.bl = bla;
        //this.router.navigate(['./stockstat', market]);
    }

onSelect() {
console.log("grr");
        this.router.navigate(['./start']);
    }
}
