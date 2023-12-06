/*
 * Copyright (C) 2023 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

const path = require('path');
const ESLintPlugin = require('eslint-webpack-plugin');
const { VueLoaderPlugin } = require('vue-loader')

const config = {
    mode: 'production',
    context: path.resolve(__dirname, '.'),
    module: {
        rules: [
            {
                test: /\.js$/,
                exclude: /node_modules/,
                use: [
                    'babel-loader',
                ]
            },
            {
                test: /\.vue$/,
                use: [
                    'vue-loader',
                ]
            }
        ]
    },
    plugins: [
        new ESLintPlugin({
            files: [
                './src/main/webapp/vue-app/*.js',
                './src/main/webapp/vue-app/*.vue',
                './src/main/webapp/vue-app/**/*.js',
                './src/main/webapp/vue-app/**/*.vue',
            ],
        }),
        new VueLoaderPlugin()
    ],
    entry: {
        glpi: './src/main/webapp/vue-app/glpi/main.js',
    },
    output: {
        path: path.join(__dirname, 'target/glpi-integration/'),
        filename: 'js/[name].bundle.js',
        libraryTarget: 'amd'
    },
    externals: {
        vue: 'Vue',
        vuetify: 'Vuetify',
        jquery: '$',
    },
};

module.exports = config;
