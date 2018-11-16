import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs/Rx';

import { MyConfig } from "../../../services/config";
import { ServiceResult } from "../../../services/serviceresult";
import { ConfigTreeMap } from "../../../services/configtreemap";
import { StockstatService } from "../../../services/stockstat-service";
import {
  FormBuilder,
    FormGroup
    } from '@angular/forms';
import {deserialize} from 'json-typescript-mapper';
    
@Component({
    selector: 'configtree-component-root',
//    selector: 'stockstat',
    templateUrl: '/src/components/builder/configtree/configtree.component.html',
})
export class ConfigTreeComponent implements OnInit{
    configo:Observable<ServiceResult>;
    configoraw:Observable<ServiceResult>;
    config:MyConfig;
    configtree:ConfigTreeMap;
    configValueMap: Map<String, Object>;
    values2: Array<ConfigTreeMap>;
    elem: ConfigTreeMap = new ConfigTreeMap();
//    values2: Map<String, ConfigTreeMap>;
//    singleintervaldays:number;
    res:ServiceResult;
    resraw:ServiceResult;
    resraw2:ServiceResult;
    name:String;
    object:Object;
    text:Map<String, String>;
    type:Map<String, String>;
    textname: String;
    typename: String;
    myvalue: String;
    
    constructor(
        public router:Router,
        public stockstatService:StockstatService) {    				console.log("here6");
}

    ngOnInit() {
    				console.log("here5");
	let sr: Observable<ServiceResult> = this.stockstatService.getMarkets();
        this.configo = this.stockstatService.getConfig();
        this.configoraw = this.stockstatService.getConfigSub();
	//let serviceresult: ServiceResult = this.stockstatService.getMarkets();
        //this.marketList = sr.subscribe(markets => console.log(markets));
	this.configo
	.subscribe(result => { this.res = result
		   console.log("this0000");
	   console.log(this.res);
	   console.log(this.res.constructor.name);
	   console.log(this.res.config);
	   console.log(this.res.config.constructor.name);
	   console.log("this0001");
}
	);
	/*
	this.configoraw
	.subscribe(result => { this.resraw = result
		   console.log("this00000");
		   console.log(result);
		   console.log(result.constructor.name);
	   this.resraw2 = new ServiceResult().deserialize(result);
	   console.log(this.resraw2)
	   console.log(this.resraw2.constructor.name)
	   console.log(this.resraw2.config)
	   console.log(this.resraw2.config.constructor.name)
	   console.log(this.resraw2.config.deflt)
	   console.log(this.resraw2.config.deflt.constructor.name)
	   //console.log(deserialize(ServiceResult, result));
	   console.log("this00001");
}
	);
	*/
	this.configoraw
	.subscribe(result => {
	//this.config = new MyConfig().deserialize(result.config);
	// TODO go for obj key?
	this.config = result.config;
	console.log("testme " + Object.values(this.config.configTreeMap));
	this.stockstatService.config = this.config;
	   console.log("this00");
	   console.log(this.config);
	   console.log(this.config.constructor.name);
	   console.log(result.config);
//	   this.config = new MyConfig().deserialize(result.config);
	   console.log(this.config);
	   console.log("this01");
	console.log(this.config.constructor.name);
	console.log(this.config.configTreeMap.constructor.name);
	console.log(this.config.configTreeMap.configTreeMap);
	//console.log(this.config.configTreeMap.configTreeMap.keys());
	//console.log(this.config.configTreeMap.configTreeMap.values());
	//console.log(this.config.configTreeMap.configTreeMap.entries());
	console.log(this.config.configTreeMap.configTreeMap.constructor.name);
	//this.values2 = Array.from(this.config.configTreeMap.configTreeMap.values());
	console.log("h00");
	// TODO same
	this.values2 = Object.values(this.config.configTreeMap.configTreeMap)
	console.log("h01");
	this.text = this.config.text;
	this.type = this.config.type;
	console.log("h015");
	//this.name = this.config.configtree.name;
	// TODO same
	this.name = this.config.configTreeMap.name;
	console.log("h02");
	//this.textname = this.text.get(name);
	//this.typename = this.type.get(name);
	// TODO same
	this.typename = this.type[this.name];
	this.textname = this.text[this.name];
	console.log("h03");
	this.configValueMap = this.config.configValueMap;
	console.log("h04");
	//this.myvalue = this.configValueMap.get(name);
	// TODO same
	console.log("valuemap " + this.configValueMap);
	this.myvalue = this.configValueMap[this.name];
	console.log("name " + this.name);
	console.log(Object.keys(this.text));
	console.log("text " + this.textname);
	console.log("type " + this.typename);
	console.log("value " + this.myvalue);
	console.log("values");
	console.log(this.values2);
	console.log(this.values2.values());
}
);
/*
	this.configo
	.subscribe(result => {
	this.configValueMap = result.config.configValueMap;
	this.configtree = result.config.configTreeMap;
	this.name = this.configtree.name;
	this.text = this.config.text;
	console.log("text " + this.text);
	//this.object = this.configValueMap.get(this.name)
	   console.log("this000");
	   console.log(typeof result);
	   console.log(typeof this.config);
	   console.log(this.config);
	   console.log(this.configtree);
	   console.log(this.configtree.name);
	   console.log(typeof this.configtree.name);
	   console.log(this.configtree.configTreeMap);
	   console.log(typeof this.configtree.configTreeMap);
	   //let bla: Map<String, String>() = new Map();
	   //console.log(bla.constructor.name);
	   //console.log(this.configValueMap.entries());
	   //for (var x in this.configValueMap) {
	   //console.log(x);
	   //console.log(this.configValueMap.get(x));
	   //}
	   console.log(typeof this.configValueMap);
	   console.log(this.configValueMap);
	   console.log(this.object);
	   console.log(typeof this.text);
	   console.log("this001");
	   var myMap = new Map();
	   // Add new elements to the map
myMap.set('bar', 'foo');
myMap.set(1, 'foobar');
	     console.log(myMap);
	   console.log(typeof myMap);
	   console.log(myMap.constructor);
	   console.log(myMap.constructor.name);
	   console.log(myMap.keys());
	   console.log(myMap.values());
	   console.log("mapend");
}
);
*/
	//console.log("h111");
	//console.log(this.res);
	//console.log("h1.55");
	//console.log(this.res.config);
	//console.log(this.configtree);
	console.log("h220");
//	this.configo
//	   .subscribe(result => this.config =result.config);
//	this.configo
//	   .subscribe(result => this.singleintervaldays =result.config.mytableintervaldays);
//	   this.singleintervaldays = this.config.mytableintervaldays;
	
    }

    onSelect() {
        this.router.navigate(['./start']);
    }
}
