import gulp from "gulp";
import config from "../config";
import webpack from "webpack";
import webpackConfig from "../../webpack.config.js";
import del from "del";
import path from "path";
import argv from "minimist";
import log from "fancy-log";
import jest from "gulp-jest";

const env = argv( process.argv.slice( 2 ) );

const eslint = require( "gulp-eslint" );

// base config, to be extended
let myConfig = Object.create( webpackConfig );

function clearBuildFolder( callback ) {
    callback();
    const filesToKeep = config.js.webpack.keepFiles.map( file => '!' + path.join( config.js.dest, file ) );
    return del( [path.join( config.js.dest, '*.js' ), ...filesToKeep], {"force": true} );
}

function executeJest( callback, outerWatch ) {
    if ( !env.skipTests && !env.skipTest ) {
        console.log( "Executing JEST javascript tests" );

        process.env.NODE_ENV = 'test';

        const task = gulp.src( config.js.src )
                .pipe( jest( {
                                 watch: Boolean( env.watch && !outerWatch ),
                                 roots: ["<rootDir>/src/js/"],
                                 testPathIgnorePatterns: [
                                     "/node_modules/",
                                     "/lib/",
                                     "/polyfills/"
                                 ]
                             } ) );
        if ( outerWatch ) {
            task.on( 'error', e => { /* suppress errors in outer watch mode */
            } );
        }

        return task;
    }

    callback();
}

gulp.task( 'js:test', executeJest );

gulp.task( "js:lint", function () {
    if ( env.debug ) {
        console.log( "linting " + config.js.lint );
    }

    return gulp.src( config.js.lint )
            .pipe( eslint() )
            // eslint.format() outputs the lint results to the console.
            // Alternatively use eslint.formatEach() (see Docs).
            .pipe( eslint.format() )
            // To have the process exit with an error code (1) on
            // lint error, return the stream and pipe to failAfterError last.
            .pipe( eslint.failAfterError() );
} );

gulp.task( "js", gulp.series( "js:lint", clearBuildFolder, function compile( callback ) {
    // if we are production, single compile
    // if we are dev, watch with sourcemaps

    if ( env.watch ) {
        myConfig.watch = true;
    }
    else {
        callback();
    }

    if ( env.production ) {
        myConfig.devtool = "";
        myConfig.plugins = myConfig.plugins.concat(
                new webpack.DefinePlugin( {
                                              "process.env": {
                                                  // This has effect on the react lib size
                                                  "NODE_ENV": JSON.stringify( "production" )
                                              }
                                          } ),
                new webpack.optimize.UglifyJsPlugin( {
                                                         mangle: true,
                                                         compress: {
                                                             sequences: true,  // join consecutive statemets with the “comma operator”
                                                             properties: true,  // optimize property access: a["foo"] → a.foo
                                                             dead_code: true,  // discard unreachable code
                                                             drop_debugger: true,  // discard “debugger” statements
                                                             unsafe: false, // some unsafe optimizations (see below)
                                                             conditionals: true,  // optimize if-s and conditional expressions
                                                             comparisons: true,  // optimize comparisons
                                                             evaluate: true,  // evaluate constant expressions
                                                             booleans: true,  // optimize boolean expressions
                                                             loops: true,  // optimize loops
                                                             unused: false,  // drop unused variables/functions
                                                             hoist_funs: true,  // hoist function declarations
                                                             hoist_vars: false, // hoist variable declarations
                                                             if_return: true,  // optimize if-s followed by return/continue
                                                             join_vars: true,  // join var declarations
                                                             cascade: true,  // try to cascade `right` into `left` in sequences
                                                             side_effects: true,  // drop side-effect-free statements
                                                             warnings: false,  // warn about potentially dangerous optimizations/code
                                                             global_defs: {}     // global definitions
                                                         }
                                                     } )
        );

    }
    else {
        myConfig.plugins = myConfig.plugins.concat(
                new webpack.LoaderOptionsPlugin( {
                                                     debug: true
                                                 } ) );
        myConfig.cache = false;
    }

    myConfig.entry = {};
    config.js.webpack.entries.map( file => myConfig.entry[file] = path.join( config.js.src, file ) );

    myConfig.externals = Object.assign( {}, config.js.webpack.externals );

    // run webpack
    return webpack( myConfig, function ( err, stats ) {

        if ( err ) {
            throw new gutil.PluginError( "webpack:build", err );
        }

        executeJest( function () {
            /* callback; unit tests done here */
        }, env.watch );

        log.info( "[webpack:build]", stats.toString( {colors: true} ) );
    } );

} ) );

