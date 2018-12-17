/* eslint-disable no-undef */

function convert(array) {
  console.log(typeof array);
  var myArray = [];
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

const ConvertToSelect = { convert };
export default ConvertToSelect;
