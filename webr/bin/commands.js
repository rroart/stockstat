const shell = require('shelljs');
const config = require('config');
const colors = require('colors');

const host = config.get('host') || 'localhost';
const port = config.get('port') || '8080';

const option = process.argv[2];

switch (option) {
  case 'lint':
    shell.exec('./node_modules/.bin/cross-env ./node_modules/.bin/eslint src/js/** server/** --format node_modules/eslint-friendly-formatter . --ext .js --ext .jsx  --cache; exit 0');
    break;
  case 'dev':
    shell.exec(`./node_modules/.bin/cross-env HOST=${host} PORT=${port} ./node_modules/.bin/webpack-dev-server --config webpack.config.dev-server.babel.js --hot --progress`);
    break;
  case 'build':
    shell.exec(`./node_modules/.bin/cross-env rimraf docroot && ./node_modules/.bin/webpack --config webpack.config.build.babel.js --progress`);
    break;
  default:
    // If the app type is invalid, stop execution of the file.
    console.log(colors.green('Invalid option.'));
    console.log(colors.green('See README.md for more details.'));
    return;
}
