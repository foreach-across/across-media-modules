/**
 * Main configuration file for frontend build parameters.
 */
import path from 'path';

/**
 * Global variables - base output directory.
 * Relative directories are relative to working directory, usually the directory containing this config.js.
 */
const outputDir = '../resources/views/static/FileManagerModule';

/**
 * Export global configuration.
 */
export default {
    "css": {
        "outputDir": path.join( outputDir, '/css' )
    },
    "js": {
        "outputDir": path.join( outputDir, '/js' ),
        "webpack": {
            //
            // Javascript files that should be bundled by webpack and copied to the output.
            // Only use filename of files that are in the root src/js folder.
            //
            "entries": [
                "file-upload"
            ],
            //
            // List of Javascript files in the output directory that should be kept and never deleted
            // when creating the webpack bundles.
            //
            "keepFiles": [
            ],
            //
            // External dependencies, usually through provided through CDN and not to be bundled.
            // Format:
            //   dependency: globalVariable
            // Example:
            //   "react": "React"
            //   Import 'react' is available as global var React.
            //
            "externals": {
                "jquery": "jQuery",
                "lodash": "_",
            }
        }
    }
}