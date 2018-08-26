import $ from 'jquery';

let selectedTemplate;

function getElementHtml( fileInput ) {
    return $( fileInput ).get( 0 ).outerHTML;
}

let onRemove = "";
const onSelect = function( selector, html ) {
    const self = $( selector );
    self.find( "input[type=file]" )
            .on( 'change', ( event ) => {
                const fileInput = $( event.target );
                const {files} = fileInput[0];
                if ( files.length > 0 ) {
                    const file = files[0];
                    fileInput.addClass( "hidden" );
                    self.append( selectedTemplate.replace( "replaceByName", file.name ) );
                    onRemove( self, html );
                }
            } );
};

onRemove = function( selector, html ) {
    const self = $( selector );
    self.find( "a" )
            .click( ( event ) => {
                self.empty();
                self.append( html );
                onSelect( selector, html );
            } );
};

EntityModule.registerInitializer( ( node ) => {
    $( ".js-file-reference-control" ).each(
            ( index, value ) => {
                const self = $( value );
                const fileInput = self.children( "input[type=file]" );
                const script = self.find( "script" );
                selectedTemplate = script.html();
                script.remove();
                let html = getElementHtml( fileInput );
                if ( fileInput.hasClass( "hidden" ) ) {
                    fileInput.removeClass( "hidden" );
                    html = getElementHtml( fileInput );
                    fileInput.remove();
                    onRemove( value, html );
                }
                else {
                    onSelect( value, html );
                }
            }
    );
} );
