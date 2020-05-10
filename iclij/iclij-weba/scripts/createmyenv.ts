import { writeFile } from 'fs';

// Configure Angular `environment.ts` file path
const targetPath = './src/environments/myenvironment.ts';

// Load node modules
const colors = require('colors');
require('dotenv').config();

// `myenvironment.ts` file structure
const envConfigFile = `export const environment = {
   MYSERVER: '${process.env.MYSERVER}',
   MYPORT: '${process.env.MYPORT}',
   MYISERVER: '${process.env.MYISERVER}',
   MYIPORT: '${process.env.MYIPORT}'
};
`;
console.log(colors.magenta('The file `myenvironment.ts` will be written with the following content: \n'));
console.log(colors.grey(envConfigFile));

writeFile(targetPath, envConfigFile, function (err) {
   if (err) {
       throw console.error(err);
   } else {
       console.log(colors.magenta(`Angular myenvironment.ts file generated correctly at ${targetPath} \n`));
   }
});
