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
                              plugins: 'autoresize image contextmenu table hr pagebreak code codesample noneditable lists advlist link paste textcolor charmap anchor',
                              toolbar1: 'undo redo | insert | styleselect | bold italic forecolor backcolor ' +
                              '| outdent indent | bullist numlist | link image codesample | pastetext',
                              insert_button_items: 'image link inserttable | pagebreak hr anchor charmap',
                              autoresize_bottom_margin: 0,
                              statusbar: false,
                              branding: false,
                              menubar: false,
                              object_resizing: 'img',
                              relative_urls: false,
                              remove_script_host: true,
                              // todo: document_base_url: "http://localhost:8080",
                              content_css: [
                                  AcrossWebModule.staticPath + '/WebCmsModule/css/wcm-tinymce-content.css'
                              ],
                              codesample_languages: [
                                  {text: 'C#', value: 'csharp'},
                                  {text: 'CSS', value: 'css'},
                                  {text: 'HTML/XML', value: 'markup'},
                                  {text: 'Java', value: 'java'},
                                  {text: 'JavaScript', value: 'javascript'},
                                  {text: 'JSON', value: 'json'},
                                  {text: 'PHP', value: 'php'},
                                  {text: 'Properties file', value: 'properties'},
                                  {text: 'Python', value: 'python'},
                                  {text: 'YAML', value: 'yaml'}
                              ],
                              file_picker_types: 'image',
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
                              },
                              setup: function ( theEditor ) {
                                  theEditor.on( 'focus', function () {
                                      $( this.contentAreaContainer.parentElement )
                                              .toggleClass( 'wcm-mce-toolbar-hidden', false )
                                              .find( "div.mce-toolbar-grp" ).show().sticky( {topSpacing: 60} );
                                  } );
                                  theEditor.on( 'blur', function () {
                                      theEditor.selection.collapse();
                                      $( this.contentAreaContainer.parentElement ).toggleClass( 'wcm-mce-toolbar-hidden', true ).find(
                                              "div.mce-toolbar-grp" ).hide().unstick();
                                  } );
                                  theEditor.on( "init", function () {
                                      $( this.contentAreaContainer.parentElement ).toggleClass( 'wcm-mce-toolbar-hidden', true ).find(
                                              "div.mce-toolbar-grp" ).hide().unstick();
                                  } );
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