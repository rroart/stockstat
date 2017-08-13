interface Serializable<T> {
    deserialize(input: Object): T;
    serialize(input: Object): T;
}

export class ConfigTreeMap implements Serializable<ConfigTreeMap> {
   constructor(
	public name: String,
	public enabled: boolean,
	public configTreeMap: Map<String, ConfigTreeMap>
   ){}

   deserialize(input) {
//console.log("thhh00");
   this.name = input.name;
   this.enabled = input.enabled;
//console.log("thhh01");
//console.log(input.name);
//console.log(input.predictors);
//console.log(input.configTreeMap);
//console.log("thhh015");
if (input.configTreeMap) {
/*   this.configTreeMap = input.configTreeMap.map(input.configTreeMap, (key) =>
   {
   console.log("thth");
   console.log(key);
   return key;
   });
   */
   this.configTreeMap = new Map<String, ConfigTreeMap>();
//console.log("thhh011111");
   Object.keys(input.configTreeMap).forEach(key =>
   {
   //console.log(key);
   this.configTreeMap.set(key, new ConfigTreeMap().deserialize(input.configTreeMap[key]));
   });
   //new ConfigTreeMap().deserialize(input.configTreeMap);
}
//console.log("thhh02");
   return this;
   }

serialize(input) {
let obj = Object.create(null);
obj["name"] = input.name;
obj["enabled"] = input.enabled;
if (input.configTreeMap) {
obj["configTreeMap"] = this.strMapToObj(input.configTreeMap);
}
//console.log("obj " + JSON.stringify(obj));
return obj;
}
strMapToObj(strMap) {
    let obj = Object.create(null);
    strMap.forEach((value: ConfigTreeMap, key: string) => {
    //for (let [k,v] of strMap) {
        // We don’t escape the key '__proto__'
        // which can cause problems on older engines
	console.log("kv " + key + " " + value + " " + value.constructor.name);
        obj[key] = value.serialize(value);
    });
    return obj;
}

strMapToObj2(strMap) {
    let obj = Object.create(null);
    for (let [k,v] of strMap) {
        // We don’t escape the key '__proto__'
        // which can cause problems on older engines
        obj[k] = v.serialize(v);
    }
    return obj;
}

}