/* global BootstrapUiModule */
import $ from 'jquery';

function getHtmlForElement( fileInput ) {
    return $( fileInput ).get( 0 ).outerHTML;
}

function retrieveSelectedFileTemplate( control ) {
    const script = $( BootstrapUiModule.refTarget( control.find( '[data-role=selected-item-template]' ) ) );
    const selectedTemplate = script.html();
    script.remove();
    return selectedTemplate;
}

function registerSingleFileUploadControl( control ) {
    const selectedFileTemplate = retrieveSelectedFileTemplate( control );
    const fileInput = control.find( ':file' );

    function registerRemoveHandler( container ) {
        container.find( 'a.remove-file' ).on( 'click', ( e ) => {
            e.preventDefault();
            $( e.target ).closest( '.file-reference-control-item' ).remove();
            fileInput.removeAttr( 'data-id' );
            fileInput.attr( 'type', 'hidden' );
            fileInput.removeAttr( 'value' );
            fileInput.attr( 'type', 'file' );
            fileInput.removeClass( 'd-none' );
        } );
    }

    if ( fileInput.data( 'id' ) ) {
        fileInput.attr( 'type', 'hidden' );
        fileInput.attr( 'value', fileInput.data( 'id' ) );
    }

    registerRemoveHandler( control );

    fileInput.on( 'change', () => {
        const {files} = fileInput[0];
        if ( files.length > 0 ) {
            const file = files[0];
            fileInput.addClass( "d-none" );
            control.append( selectedFileTemplate.replace( "{{fileName}}", file.name ) );
            registerRemoveHandler( control );
        }
    } );
}

function registerMultiFileUploadControl( control ) {
    const selectedFileTemplate = retrieveSelectedFileTemplate( control );
    const firstFileInput = control.find( '[data-role=file-upload-template]' ).first();
    const fileInputTemplate = getHtmlForElement( firstFileInput );

    let nextItemId = control.data( 'next-item-id' );

    function registerRemoveHandlers( container ) {
        container.find( 'a.remove-file' ).on( 'click', ( e ) => {
            e.preventDefault();
            $( e.target ).closest( '.file-reference-control-item' ).remove();
        } );
    }

    function registerFileSelectedHandler( fileInput ) {
        fileInput.on( 'change', () => {
            const sortIndex = nextItemId++;
            const itemId = `item-${sortIndex}`;
            const newFiles = fileInput[0].files;

            if ( newFiles.length > 0 ) {
                const name = fileInput.data( 'control-name' ).replace( /{{key}}/g, itemId );
                fileInput.addClass( 'd-none' );
                fileInput.removeAttr( 'data-role' );
                fileInput.removeAttr( 'data-control-name' );
                fileInput.attr( 'name', name );

                for ( let i = 0; i < newFiles.length && i < 1; i++ ) {
                    const file = newFiles[i];
                    const newFile = $( selectedFileTemplate.replace( "{{fileName}}", file.name ) );
                    control.append( newFile );
                    newFile.append( fileInput );

                    const sortIndexValue = $( '<input type="hidden" />' );
                    sortIndexValue.prop( 'name', fileInput.attr( 'name' ).replace( /.value$/g, '.sortIndex' ) );
                    sortIndexValue.prop( 'value', sortIndex );
                    newFile.append( sortIndexValue );

                    registerRemoveHandlers( newFile );
                }

                const newFileInput = $( fileInputTemplate );
                control.append( newFileInput );

                registerFileSelectedHandler( newFileInput );
            }
        } );
    }

    registerRemoveHandlers( control );
    registerFileSelectedHandler( firstFileInput );
}

EntityModule.registerInitializer( ( container ) => {
    $( ".js-file-reference-control", container ).each(
            ( index, node ) => {
                const self = $( node );

                if ( self.data( 'multiple' ) ) {
                    registerMultiFileUploadControl( self );
                }
                else {
                    registerSingleFileUploadControl( self );
                }
            }
    );
} );
