const path = require('path');

module.exports = {
    entry: {
        main: './src/PassStorageMainApp.js',
        // downloads: './src/main/js/DownloadsApp.js'
    },
    cache: false,
    mode: 'development',
    devtool: 'source-map',
    output: {
        filename: '[name].js',
        path: path.resolve(__dirname, '../../src/main/resources/static/built/'),
        clean: true,
    },
    watchOptions: {
        poll: true,
        ignored: /node_modules/
    },
    module: {
        rules: [
            {
                test: /\.(?:js|mjs|cjs)$/,
                exclude: /node_modules/,
                use: {
                    loader: 'babel-loader',
                    options: {
                        presets: [
                            ['@babel/preset-env', { targets: "defaults" }],
                            ["@babel/preset-react", { targets: "defaults" }]
                        ]
                    }
                }
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]

            },
            {
                test: /\.(png|svg|jpg|gif|eot|otf|ttf|woff|woff2)$/,
                use: [
                    {
                        loader: 'url-loader',
                        options: {}
                    }
                ]
            }
        ]
    }
};