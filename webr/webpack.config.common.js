// Requiring dependencies
// ================================================================================
const path = require('path');
const webpack = require('webpack');
const ExtractTextPlugin = require('mini-css-extract-plugin');
const CaseSensitivePathsPlugin = require('case-sensitive-paths-webpack-plugin');
const config = require('config');

// trace which loader is deprecated
// feel free to remove that if you don't need this feature
process.traceDeprecation = false;

// Environment variable injection
// ================================================================================
const packageJSON = require('./package.json');
process.env.PACKAGE_VERSION = packageJSON.version

// Defining config variables
// ================================================================================

const BUILD_PATH = path.join(__dirname, `docroot${config.get('publicPath')}`)

const COMMON_LOADERS = [
  {
    test: /\.(?:ico|gif|png|jpg|jpeg|webp|svg)$/i,
    use: [
      {
        loader: 'file-loader',
        options: {
          hash: 'sha512',
          digest: 'hex',
          name: `${config.get('assetPath')}/[contenthash].[ext]`,
        }
      },
      {
        loader: 'image-webpack-loader',
        options: {
          query: {
            mozjpeg: {
              progressive: true,
            },
            gifsicle: {
              interlaced: true,
            },
            optipng: {
              optimizationLevel: 7,
            },
            pngquant: {
              quality: '65-90',
              speed: 4
            }
          },
        }
      }
    ],
  }, {
    test: /\.(js|jsx|ts|tsx)?$/,
    exclude: /node_modules/,
    loader: 'babel-loader',
    options: {
	cacheDirectory: true,
	presets: [
                            ["@babel/preset-env", { 
                                "targets": "defaults"
                            }]
                        ],
      plugins: [
        '@babel/transform-runtime',
        ['@babel/plugin-proposal-decorators', { "legacy": true }],
          '@babel/syntax-dynamic-import',
      ],
    },
  },
  {
    test: /\.woff(\?v=\d+\.\d+\.\d+)?$/,
    use: [
      {
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'application/font-woff',
          name: `${config.get('assetPath')}/[name].[ext]`,
        }
      }
    ],
  },
  {
    test: /\.woff2(\?v=\d+\.\d+\.\d+)?$/,
    use: [
      {
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'application/font-woff',
          name: `${config.get('assetPath')}/[name].[ext]`,
        }
      }
    ],
  },
  {
    test: /\.[ot]tf(\?v=\d+\.\d+\.\d+)?$/,
    use: [
      {
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'application/octet-stream',
          name: `${config.get('assetPath')}/[name].[ext]`,
        }
      }
    ],
  },
  {
    test: /\.eot(\?v=\d+\.\d+\.\d+)?$/,
    use: [
      {
        loader: 'url-loader',
        options: {
          limit: 10000,
          mimetype: 'application/vnd.ms-fontobject',
          name: `${config.get('assetPath')}/[name].[ext]`,
        }
      }
    ],
  }
];

const { CycloneDxWebpackPlugin } = require('@cyclonedx/webpack-plugin');

/** @type {import('@cyclonedx/webpack-plugin').CycloneDxWebpackPluginOptions} */
const cycloneDxWebpackPluginOptions = {
  specVersion: '1.6',
  outputLocation: './bom'
}

// Export
// ===============================================================================
const JS_SOURCE = config.get('jsSourcePath');

module.exports = {
  output: {
    path: path.join(__dirname, 'docroot'),
  },
  resolve: {
      extensions: ['.js', '.jsx', '.ts', '.tsx', '.css'],
      fallback: {
	  fs: false
      },
    modules: [
      path.join(__dirname, 'src'),
      path.join(__dirname, 'assets'),
      path.join(__dirname, JS_SOURCE),
      "node_modules"
    ],
  },
  plugins: [
      new webpack.IgnorePlugin({ resourceRegExp: /vertx/}), // https://github.com/webpack/webpack/issues/353
      new CaseSensitivePathsPlugin(),
      new CycloneDxWebpackPlugin(cycloneDxWebpackPluginOptions),
  ],
  module: {
    rules: COMMON_LOADERS,
  },
  externals: {
    console:true,
    fs:'{}',
    tls:'{}',
    net:'{}'
  },
};
