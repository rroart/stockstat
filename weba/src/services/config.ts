import {Injectable} from '@angular/core';

import {ConfigTreeMap} from './configtreemap';

interface Serializable<T> {
    deserialize(input: Object): T;
    serialize(input: Object): T;
}

@Injectable()
export class MyConfig implements Serializable<MyConfig> {
  constructor(
    public mydate: Date,
    public mymarket: String,
    public configTreeMap: ConfigTreeMap,
    public configValueMap: Map<String, Object>,
    public text: Map<String, String>,
    public deflt: Map<String, Object>,
    public type: Map<String, String>
) {}

deserialize(input) {
console.log("thh00");
this.mydate = input.mydate;
this.mymarket = input.mymarket;
if (input.configTreeMap) {
this.configTreeMap = new ConfigTreeMap().deserialize(input.configTreeMap);
}
console.log("thh01");
this.configValueMap = new Map<String, String>();
Object.keys(input.configValueMap).forEach(key => { console.log("key"+key); this.configValueMap.set(key, input.configValueMap[key])});
this.text = new Map<String, String>();
Object.keys(input.text).forEach(key => this.text.set(key, input.text[key]));
this.deflt = new Map<String, String>();
Object.keys(input.deflt).forEach(key => this.deflt.set(key, input.deflt[key]));
// Class
this.type = new Map<String, String>();
Object.keys(input.type).forEach(key => this.type.set(key, input.type[key]));

return this;
}

serialize(input) {
let obj = Object.create(null);
obj["mydate"] = input.mydate;
obj["mymarket"] = input.mymarket;
console.log("here0"+input.configTreeMap);
if (input.configTreeMap) {
obj["configTreeMap"] = input.configTreeMap.serialize(input.configTreeMap);
}
console.log("here1"+input.configValueMap);
if (input.configValueMap) {
obj["configValueMap"] = this.strMapToObj(input.configValueMap);
}
console.log("here2");
if (input.text) {
obj["text"] = this.strMapToObj(input.text);
}
console.log("here3");
if (input.deflt) {
obj["deflt"] = this.strMapToObj(input.deflt);
}
console.log("here3");
if (input.type) {
obj["type"] = this.strMapToObj(input.type);
}
console.log("here4");
    return obj;
}

strMapToObj2(strMap) {
    let obj = Object.create(null);
    for (let [k,v] of strMap) {
        // We don’t escape the key '__proto__'
        // which can cause problems on older engines
        obj[k] = v;
    }
    return obj;
}

strMapToObj(strMap : Map<String, String>) {
		   //console.log("str " + strMap);
    let obj = Object.create(null);
    //let strMap2: Map<String, String> = strMap;
    strMap.forEach((value: string, key: string) => {
    //for (let entry of Array.from(strMap.entries())) {
        // We don’t escape the key '__proto__'
        // which can cause problems on older engines
	    //let key = entry[0];
    //let value = entry[1];
        obj[key] = value;
    });
    //}
    return obj;
}

	      }
	      