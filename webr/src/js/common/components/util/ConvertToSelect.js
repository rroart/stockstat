/* eslint-disable no-undef */

function convert(array) {
    console.log("here");
    //console.log(array);
  //console.log(typeof array);
    var myArray = [];
    //console.log(array.length)
  for(var i in array) {
    var item = {
      label: array[i],
      value: array[i]
    }
    myArray.push(item);
  }
  //console.log(typeof myArray);
  //console.log(myArray);
  return myArray;
}

function convert2(list) {
    console.log("here");
    //console.log(list);
  //console.log(typeof list);
    var myArray = [];
    //console.log(list.size)
    for(var i = 0; i < list.size; i++) {
	//console.log(list.get(i));
    var item = {
	label: list.get(i),
	value: list.get(i),
    }
    myArray.push(item);
  }
  //console.log(typeof myArray);
  //console.log(myArray);
  return myArray;
}

function convert3(list) {
  console.log("here");
  //console.log(list);
  //console.log(typeof list);
  var myArray = [];
  //console.log(list.size)
  for(let i in list) {
    //console.log(list[i]);
    var item = {
      label: list[i],
      value: list[i],
    }
    myArray.push(item);
  }
  //console.log(typeof myArray);
  //console.log(myArray);
  return myArray;
}

const ConvertToSelect = { convert, convert2, convert3 };
export default ConvertToSelect;
