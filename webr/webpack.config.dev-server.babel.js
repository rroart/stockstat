import config from 'config';
import webpack from 'webpack';
import HtmlWebpackPlugin from 'html-webpack-plugin';
import DashboardPlugin from 'webpack-dashboard/plugin';
import precss from 'precss';
import postcssCssnext from 'postcss-cssnext';

const webpackConfig = require('./webpack.config.common');
const JS_SOURCE = config.get('jsSourcePath');

// Please read the following link if
// you have no idea how to use this feature
// https://github.com/motdotla/dotenv
require('dotenv').config({ silent: true });

const HOST = process.env.HOST || config.get('host') || '0.0.0.0'
const PORT = process.env.PORT || config.get('port') || '8080'
const APP_ENTRY_POINT = `${JS_SOURCE}/router`;

const webpackDevOutput = {
  publicPath: config.get('publicPath'),
  filename: 'bundle.js',
};

// Merges webpackDevOutput and webpackConfig.output
webpackConfig.output = Object.assign(webpackConfig.output, webpackDevOutput);

webpackConfig.devServer = {
  host: HOST,
  port: PORT,
  compress: true,
  open: true,
};

webpackConfig.mode = 'development';

// This is your testing container, we did
// that for you, so you don't need to, if
// you need to change the container template
// go to the file in `template` below
const html = config.get('html');

const htmlPlugins = html.map((page) =>
  new HtmlWebpackPlugin({
    title: page.title,
    template: `src/assets/template/${page.template}`,
    inject: 'body',
    filename: page.filename,
  }));

webpackConfig.plugins.push(
  new DashboardPlugin({
    port: process.env.DASHBOARD_PORT,
    minified: false,
    gzip: false,
  }),
  new webpack.LoaderOptionsPlugin({
    debug: true
  }),
  // Since we specify --hot mode, we don’t need to add this plugin
  // It is mutually exclusive with the --hot option.
  // new webpack.HotModuleReplacementPlugin(),
  new webpack.DefinePlugin({
    __CONFIG__: JSON.stringify(config.get('app')),
    'process.env': {
      NODE_ENV: JSON.stringify('development')
    },
  }),
);

// We turn off browserSync by default
// Turn that on if you want to include this use case
if (config.get('browserSync.active') === true) {
  const BrowserSyncPlugin = require('browser-sync-webpack-plugin');
  webpackConfig.plugins.push(new BrowserSyncPlugin({
    host: 'localhost',
    port: config.get('browserSync.port'),
    proxy: `http://localhost:${process.env.PORT}/`,

    // Prevents BrowserSync from automatically opening up the app in your browser
    open: false,
    reloadDelay: 2500,
  }, {
    // Disable BrowserSync's browser reload/asset injections feature because
    // Webpack Dev Server handles this for us already
    reload: false,
  }));
}

webpackConfig.module.rules = webpackConfig.module.rules.concat({
  test: /\.css$/,
  use: [
    {
      loader: 'style-loader',
    },
    {
      loader: 'css-loader',
      options: { sourceMap: true, importLoaders: 1 }
    },
    {
      loader: 'postcss-loader',
      options: {
        sourceMap: true,
        // https://github.com/postcss/postcss-loader/issues/92
        // https://github.com/postcss/postcss-loader/issues/8
      },
    },
  ],
});

webpackConfig.plugins = webpackConfig.plugins.concat(htmlPlugins);

webpackConfig.devtool = 'eval-cheap-module-source-map';

webpackConfig.entry = [
  'babel-polyfill',
  `webpack-dev-server/client?http://${HOST}:${PORT}`,
  'webpack/hot/only-dev-server',
  `./${APP_ENTRY_POINT}`,
];

export default webpackConfig;
