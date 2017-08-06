/*
 * Copyright 2017 the original author or authors
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
(function ( $ ) {
    EntityModule.registerInitializer( function ( node ) {
        $( '[data-wcm-markup-type=rich-text]', node ).each( function () {
            /*
             CKEDITOR.disableAutoInline = true;
             // { extraPlugins: 'autogrow', autoGrow_minHeight : 300, autoGrow_onStartup: true }
             CKEDITOR.inline( $( this ).attr( 'id' ), {} );
             */
            var id = '[id="' + $( this ).attr( 'id' ) + '"]';
            tinymce.init( {
                              selector: id,
                              plugins: 'autoresize image contextmenu table hr pagebreak code codesample noneditable advlist',
                              autoresize_bottom_margin: 0,
                              statusbar: false,
                              branding: false,
                              menubar: false,
                              toolbar2: 'codesample',
                image_caption: true,
                              /* content_css: [
                               AcrossWebModule.staticPath + '/adminweb/css/admin-web-bootstrap.css'
                               ],*/
                              file_picker_callback: function ( callback, value, meta ) {
                                  // Provide image and alt text for the image dialog
                                  if ( meta.filetype === 'image' ) {
                                      WebCmsModule.imageSelector( {
                                                                      outputBox: {width: 800, height: 800},
                                                                      callback: function ( image ) {
                                                                          callback( image.url, {'data-wcm-object-id': image.imageId} );
                                                                      }
                                                                  } );
                                  }
                              }
                          } );
        } );

        $( '[data-wcm-markup-type=markup]', node ).each( function () {
            var cm = CodeMirror.fromTextArea( $( this )[0], {lineNumbers: true, mode: 'htmlmixed', viewportMargin: Infinity} );
            $( this ).on( 'wcm:componentRefresh',
                          function () {
                              cm.refresh();
                          } )
                    .attr( 'data-wcm-component-refresh', 'true' );
        } );
    } );
})( jQuery );