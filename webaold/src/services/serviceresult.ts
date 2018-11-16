import {Injectable} from '@angular/core';

import { MyConfig } from './config';

//import {deserialize} from 'json-typescript-mapper';
//import { serialize, deserialize, JsonProperty, JsonObject } from 'json-typescript-mapper';

interface Serializable<T> {
    deserialize(input: Object): T;
}

interface Serializable2<T> {
    serialize(input: T): Object;
}

@Injectable()
//@JsonObject
export class ServiceResult implements Serializable<ServiceResult> {
/*
  @JsonProperty('config')
public config: MyConfig;
@JsonProperty('markets')
public markets: String[];
@JsonProperty('error')
public error: string;
*/
constructor(
public config: MyConfig,
public markets: String[],
//public Map<String, String> stocks,
//public List<ResultItem> list,
public error: string
) {}
/*
constructor() {
this.config = undefined;
this.markets = undefined;
this.error = undefined;
}
*/
deserialize(input) {
console.log("th00");
console.log(input);
console.log(input.config);
this.config = new MyConfig().deserialize(input.config);
console.log("MyCo");
console.log(this.config);
console.log(this.config.constructor.name);
this.markets = input.markets;
this.error = input.error;
return this;
}

}

//@Injectable
export class ServiceParam implements Serializable2<ServiceParam> {
constructor(
public config: MyConfig,
//public GUISize guiSize,
public ids: String[],
public market: String) {}

serialize(input) {
let obj = Object.create(null);
obj["markets"] = input.markets;
obj["error"] = input.error;
obj["config"] = input.config.serialize(input.config)
return obj;
}
}
