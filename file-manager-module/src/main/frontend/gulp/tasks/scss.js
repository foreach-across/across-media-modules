/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import gulp from "gulp";
import handleErrors from "../util/handleErrors";
import config from "../config";
import sass from "gulp-sass";
import sourcemaps from "gulp-sourcemaps";
import autoprefixer from "gulp-autoprefixer";
import gulpif from "gulp-if";
import sassLint from "gulp-sass-lint";
import touch from "gulp-touch-fd";
import log from "fancy-log";
import colors from "ansi-colors";
import argv from "minimist";

const env = argv( process.argv.slice( 2 ) );

gulp.task( "scss:lint", function () {
    return gulp.src( config.scss.src )
            .pipe( sassLint( {configFile: config.scss.lintConfig} ) )
            .pipe( sassLint.format() )
            .pipe( sassLint.failOnError() )
} );

const AUTOPREFIXER_BROWSERS = [
    "ie >= 10",
    "ie_mob >= 10",
    "ff >= 30",
    "chrome >= 34",
    "safari >= 7",
    "opera >= 23",
    "ios >= 7",
    "android >= 4.4",
    "bb >= 10"
];

function singleRun() {
    if ( env.debug ) {
        console.log( "Compiling SCSS " + config.scss.src + " to " + config.scss.dest );
    }

    return gulp.src( config.scss.src )
            .pipe( gulpif( !env.production, sourcemaps.init() ) )
            .pipe( sass( {
                             outputStyle: env.production ? "compressed" : "expanded",
                             errLogToConsole: true,
                             sourceComments: !env.production
                         } ) )
            .on( "error", env.watch ? (e => log.error( colors.red( e.message ) )) : handleErrors )
            .pipe( autoprefixer( AUTOPREFIXER_BROWSERS ) )
            .pipe( gulpif( !env.production, sourcemaps.write() ) )
            .pipe( gulp.dest( config.scss.dest ) )
            .pipe( touch() );
}

gulp.task( "scss", gulp.series( function () {
    if ( env.watch ) {
        console.log( "WATCH COMPILE" );

        singleRun(); // always compile once
        console.log( "WATCHER - first compile done" );

        // scss
        const scssWatcher = gulp.watch( config.scss.src, singleRun );

        scssWatcher.on( "change", function ( event ) {
            log.info( colors.yellow( "WATCHER: file changed - " + event ) );
        } );

        scssWatcher.on( "error", function ( event ) {
            log.error( colors.red( "WATCHER: error - " + event ) );
            process.emit( "end" );
        } );
    }
    else {
        console.log( "SINGLE COMPILE (you can also use --watch)" );

        return singleRun();
    }
} ) );
