import { Component, OnInit, Input } from '@angular/core';
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
    selector: 'treeview',
//    selector: 'stockstat',
    templateUrl: '/src/components/builder/treeview/treeview.component.html',
})
export class TreeViewComponent implements OnInit{
@Input() text : Map<String, String>;    
@Input() type : Map<String, String>;    
@Input() elem : ConfigTreeMap;
@Input() configValueMap : Map<String, String>;
    values2: Array<ConfigTreeMap>;
    textname: String;
    typename: String;
    myvalue: String;
    checkbox: boolean = false;
    checkboxvalue: boolean;
    newvalue: String;
    //= Array.from(elem.configTreeMap.values());
/*
    configo:Observable<ServiceResult>;
    configoraw:Observable<ServiceResult>;
    config:MyConfig;
    configtree:ConfigTreeMap;
    configValueMap: Map<String, Object>;
//    singleintervaldays:number;
    res:ServiceResult;
    resraw:ServiceResult;
    resraw2:ServiceResult;
    name:String;
    object:Object;
    text:Map<String, String>;
*/

/*
    constructor(
        public router:Router,
        public stockstatService:StockstatService) {    				console.log("here6");
}
*/
    ngOnInit() {
//console.log("elem");
//console.log(this.elem);
	//this.values2 = Array.from(this.elem.configTreeMap.values());
	//this.textname = this.text.get(this.elem.name);
	console.log("elem"+this.elem);
	console.log(this.elem.constructor.name);
	console.log("elem"+Object.values(this.elem));
	// TODO same
	this.values2 = Object.values(this.elem["configTreeMap"]);
	this.textname = this.text[this.elem.name];
	if (this.textname == null) {
	   this.textname = this.elem.name;
	   let idx = this.textname.indexOf(".");
	   if (idx >= 0) {
	      this.textname = this.textname.substring(idx + 1);
	   }
	}
	//this.typename = this.type.get(this.elem.name);
	//this.myvalue = this.configValueMap.get(this.elem.name);
	this.newvalue = this.myvalue;
	// TODO same
	this.typename = this.type[this.elem.name];
	this.myvalue = this.configValueMap[this.elem.name];
	if (this.typename == "java.lang.Boolean") {
	   this.checkbox = true;
	   this.checkboxvalue = this.myvalue;
//	   console.log("boolval " + this.checkboxvalue);
	   }

    				//console.log("there5"+this.name);
    				//console.log("there5"+this.textname);
    				//console.log("there5"+this.typename);
    				//console.log("there5"+this.myvalue);
				/*
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
	this.configoraw
	.subscribe(result => {
	this.config = new MyConfig().deserialize(result.config);
	   console.log("this00");
	   console.log(this.config);
	   console.log(this.config.constructor.name);
	   console.log(result.config);
//	   this.config = new MyConfig().deserialize(result.config);
	   console.log(this.config);
	   console.log("this01");
}
);
	this.configo
	.subscribe(result => {
	this.configValueMap = result.config.configValueMap;
	this.configtree = result.config.configTreeMap;
	this.values = result.config.configTreeMap.values();
	this.name = this.configtree.name;
	this.text = this.config.text;
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
}
);
	//console.log("h111");
	//console.log(this.res);
	//console.log("h1.55");
	//console.log(this.res.config);
	//console.log(this.configtree);
	console.log("h22");
//	this.configo
//	   .subscribe(result => this.config =result.config);
//	this.configo
//	   .subscribe(result => this.singleintervaldays =result.config.mytableintervaldays);
//	   this.singleintervaldays = this.config.mytableintervaldays;
*/	
    }

/*
    get myvalue() {
    	return this.myvalue;
	}

	set myvalue(value) {
	    console.log("new val " + value);
	}
*/

    onSelect(value: String) {
    		    console.log("se " + value);
        this.router.navigate(['./start']);
    }

    onSubmit(value: String) {
    console.log("va " + this.elem.name + " " + value);
    //this.configValueMap.set(this.elem.name, value);
    // TODO same
    this.configValueMap[this.elem.name] = value;
}

    methodInsideYourComponent(value: String) {
    console.log("va2 " + value);
    console.log(this.newvalue);
}

	switchMe() {
	console.log("switched");
	}

	switchMe(event) {
	console.log("switched2"+event.target.checked);
    console.log("va3 " + this.elem.name + " " + event.target.checked);
    //this.configValueMap.set(this.elem.name, event.target.checked);
    // TODO same
    this.configValueMap[this.elem.name] = event.target.checked;
	}

}