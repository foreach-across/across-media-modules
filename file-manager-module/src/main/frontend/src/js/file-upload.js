/* global BootstrapUiModule */
import $ from 'jquery';

let selectedTemplate;

let fileIndex = 1;

function getHtmlForElement( fileInput ) {
    return $( fileInput ).get( 0 ).outerHTML;
}

let registerRemoveFileHandler = "";
const registerSingleSelectHandler = function( rootElement, html ) {
    const self = $( rootElement );
    self.find( "input[type=file]" )
            .on( 'change', ( event ) => {
                const fileInput = $( event.target );
                const {files} = fileInput[0];
                if ( files.length > 0 ) {
                    const file = files[0];
                    fileInput.addClass( "hidden" );
                    self.append( selectedTemplate.replace( "replaceByName", file.name ) );
                    registerRemoveFileHandler( self, html, $( self ).find( "a.remove-file" ) );
                }
            } );
};

const fetchLargestIndex = function( rootElement ) {
    const self = $( rootElement );
    let largestIndex = 0;
    self.find( "input[data-item-idx]" ).each( ( index, value ) => {
        const idx = parseInt( $( value ).attr( 'data-item-idx' ), 10 );
        if ( idx > largestIndex ) {
            largestIndex = idx;
        }
    } );
    return largestIndex + 1;
};

const registerMultiselectHandler = function( rootElement, html, element ) {
    const root = $( rootElement );
    const self = $( element );
    self.on( 'change', ( event ) => {
        const fileInput = $( event.target );
        const newFiles = fileInput[0].files;
        if ( newFiles.length > 0 ) {
            for ( let i = 0; i < newFiles.length; i++ ) {
                const file = newFiles[i];
                const newFile = $( selectedTemplate.replace( "replaceByName", file.name ) );
                root.append( newFile );
                registerRemoveFileHandler( self, html, $( newFile ).find( "a.remove-file" ), true );
            }
            fileInput.addClass( 'hidden' );
            const name = fileInput.data( 'control-name' ).replace( /{{key}}/g, `item-${fileIndex++}` );
            fileInput.removeData( 'control-name' );
            fileInput.removeData( 'role' );
            fileInput.attr( 'name', name );
            const newFileInput = $( html );
            root.append( newFileInput );
            registerMultiselectHandler( root, html, newFileInput );
        }
    } );
};

registerRemoveFileHandler = function( rootElement, html, element, forMultiple = false ) {
    const root = $( rootElement );
    const self = $( element );
    self.on( 'click', ( event ) => {
        event.preventDefault();
        if ( !forMultiple ) {
            root.empty();
            root.prepend( html );
            registerSingleSelectHandler( rootElement, html );
        }
        else {
            const parent = $( event.target ).closest( ".file-reference-control-item" );
            parent.remove();
        }
    } );
};

EntityModule.registerInitializer( ( node ) => {
    $( ".js-file-reference-control" ).each(
            ( index, value ) => {
                const self = $( value );

                const script = $( BootstrapUiModule.refTarget( self.find( '[data-role=selected-item-template]' ) ) );
                selectedTemplate = script.html();
                script.remove();

                const existingFiles = self.find( "a.remove-file" );

                const isForMultiple = self.data( "multiple" );

                const fileInput = isForMultiple ? self.find( '[data-role=file-upload-template]' ) : self.children( '.js-file-control' ).get( 0 );
                let html = getHtmlForElement( fileInput );

                if ( isForMultiple ) {
                    html = getHtmlForElement( fileInput );
                    $( existingFiles ).each( ( ix, file ) => {
                        registerRemoveFileHandler( value, html, file, true );
                    } );
                    registerMultiselectHandler( value, html, fileInput );
                }
                else {
                    const fileInput = self.children( ".js-file-control" );

                    if ( fileInput.attr( "data-id" ) ) {
                        html = getHtmlForElement( fileInput );
                        fileInput.attr( 'type', 'hidden' );
                        fileInput.attr( 'value', fileInput.attr( 'data-id' ) );
                        $( existingFiles ).each( ( ix, item ) => {
                            registerRemoveFileHandler( value, html, item );
                        } );
                    }
                    else {
                        registerSingleSelectHandler( value, html, fileInput );
                    }
                }
            }
    );
} );
