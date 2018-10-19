/* eslint-disable no-undef */

function convert(array) {
    console.log(array);
  console.log(typeof array);
    var myArray = [];
    console.log(array.length)
  for(var i in array) {
    var item = {
      label: array[i],
      value: array[i]
    }
    myArray.push(item);
  }
  console.log(typeof myArray);
  //console.log(myArray);
  return myArray;
}

function convert2(list) {
    console.log(list);
  console.log(typeof list);
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
  console.log(typeof myArray);
  //console.log(myArray);
  return myArray;
}

const ConvertToSelect = { convert, convert2 };
export default ConvertToSelect;
