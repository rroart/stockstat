const packageJson = require('../../package.json');

export const environment = {
  appName: 'Stockstat',
  envName: 'TEST',
  production: false,
  test: true,
  i18nPrefix: '',
  versions: {
    app: packageJson.version,
    angular: packageJson.dependencies['@angular/core'],
    ngrx: packageJson.dependencies['@ngrx/store'],
    material: packageJson.dependencies['@angular/material'],
    bootstrap: packageJson.dependencies.bootstrap,
    rxjs: packageJson.dependencies.rxjs,
    ngxtranslate: packageJson.dependencies['@ngx-translate/core'],
    fontAwesome:
      packageJson.dependencies['@fortawesome/fontawesome-free-webfonts'],
    angularCli: packageJson.devDependencies['@angular/cli'],
    typescript: packageJson.devDependencies['typescript'],
    cypress: packageJson.devDependencies['cypress']
  },
  oauth2: {
    clientId: '',
    issuer: '',
    loginUrl: '',
    tokenEndpoint: '',
    userinfoEndpoint: '',
    logoutUrl: '',
    scope: 'openid profile email',
    responseType: 'code',
    redirectUri: ''
  }
};
