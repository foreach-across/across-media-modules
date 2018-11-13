import settings from '../settings.js';
import argv from "minimist";
import path from "path";

const env = argv( process.argv.slice( 2 ) );
const workingDirectory = process.env.INIT_CWD;

let dest;

const gutil = {
    env: {
        slicing: true,
        path: ''
    }
};

const slicingConfig = {
    "dest": "./slicing/",
    "URL": "./slicing/",
    "templates": ["./slicing/**/*.html", "./slicing/css/main.css"]
};

const devConfig = {
    "dest": "../resources/views/static/entity/",
    "URL": "../resources/views/static/entity/",
    "templates": ["../foreach-boilerplate-examplesite/**/*.html", "../foreach-boilerplate-examplesite/css/main.css"]
};

if ( gutil.env.slicing ) {
    dest = slicingConfig.dest;
}
else {
    dest = devConfig.dest
}

export default {
    "root": workingDirectory,
    "dest": dest,
    "scss": {
        "lintConfig": '.sass-lint.yml',
        "src": path.join( workingDirectory, "src/scss/**/*.scss" ),
        "dest": path.join( workingDirectory, settings.css.outputDir )
    },
    "js": {
        "src": path.join( workingDirectory, "src/js" ),
        "lint": [
            path.join( workingDirectory, "src/js/**/*.js" ),
            "!" + path.join( workingDirectory, "src/js/lib/**/*.js" ),
            "!" + path.join( workingDirectory, "src/js/polyfills/**/*.js" )
        ],
        "test": path.join( workingDirectory, "src/js" ),
        "dest": path.join( workingDirectory, settings.js.outputDir ),
        "webpack": settings.js.webpack
    }
};
