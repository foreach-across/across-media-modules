import $ from 'jquery';

let selectedTemplate;

function getElementHtml( fileInput ) {
    return $( fileInput ).get( 0 ).outerHTML;
}

let onRemove = "";
const onSelect = function( rootElement, html ) {
    const self = $( rootElement );
    console.log( '=== onSelect ===' );
    self.find( "input[type=file]" )
            .on( 'change', ( event ) => {
                const fileInput = $( event.target );
                const {files} = fileInput[0];
                if ( files.length > 0 ) {
                    const file = files[0];
                    fileInput.addClass( "hidden" );
                    self.append( selectedTemplate.replace( "replaceByName", file.name ) );
                    onRemove( self, html, $( self ).find( "a.remove-file" ) );
                }
            } );
};

const onMultiSelect = function( rootElement, html, element ) {
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
                onRemove( self, html, $( newFile ).find( "a.remove-file" ), true );
            }
            fileInput.addClass( 'hidden' );
            const newFileInput = $( html );
            root.prepend( newFileInput );
            onMultiSelect( root, html, newFileInput );
        }
    } );
};

onRemove = function( rootElement, html, element, forMultiple = false ) {
    const root = $( rootElement );
    const self = $( element );
    self.on( 'click', ( event ) => {
        console.log( `isForMultiple: ${forMultiple}` );
        if ( !forMultiple ) {
            root.empty();
            root.prepend( html );
            onSelect( rootElement, html );
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
                const fileInput = self.children( ".js-file-control" );

                const script = self.find( "script" );
                selectedTemplate = script.html();
                script.remove();

                let html = getElementHtml( fileInput );
                const existingFiles = self.find( "a.remove-file" );

                if ( $( fileInput[0] ).prop( "multiple" ) ) {
                    html = getElementHtml( fileInput );
                    $( existingFiles ).each( ( ix, file ) => {
                        onRemove( value, html, file, true );
                    } );
                    onMultiSelect( value, html, fileInput );
                }
                else {
                    if ( fileInput.hasClass( "hidden" ) ) {
                        fileInput.removeClass( "hidden" );
                        html = getElementHtml( fileInput );
                        fileInput.remove();
                        $( existingFiles ).each( ( ix, item ) => {
                            onRemove( value, html, item );
                        } );
                    }
                    else {
                        onSelect( value, html, fileInput );
                    }
                }
            }
    );
} );
