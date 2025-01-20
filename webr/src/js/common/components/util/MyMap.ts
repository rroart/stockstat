function myget(object, property) {
  console.log(property);
  return object[property];
}

function myhas(object, property) {
  return object.hasOwnProperty(property);
}

function mymap(object) {
  const map = new Map();
  const keys = Object.keys(object);
  for (let i in keys) {
    const key = keys[i];
    const value = object[key];
    map.set(key, value);
  }
  return map;
}

function myset(object, property, value) {
  object[property] = value;
  return object;
}

const MyMap = { myget, myhas, mymap, myset };
export default MyMap;
