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

$( document ).on( 'ready', function () {

    CKEDITOR.on( 'dialogDefinition', function ( ev ) {
        var dialogName = ev.data.name;
        var dialogDefinition = ev.data.definition;

        if ( dialogName == 'image2' ) {
            var infoTab = dialogDefinition.getContents( 'info' );

            infoTab.add( {
                             type: 'text',
                             label: 'Image server key',
                             id: 'imageServerKey'
                         } );

            dialogDefinition.addContents(
                    {
                        id: 'customTab',
                        label: 'My tab',
                        minWith: 1024,
                        elements: [
                            {
                                id: 'iframe',
                                type: 'html',
                                html: '<iframe src="http://localhost:8080/cms"></iframe>'
                            }
                        ]
                    }
            );

            console.log(dialogDefinition.onOk);

            dialogDefinition.onOk = function(dialog) {
               console.log('ok');
               console.log(dialog);
            };
        }
    } );

    $( 'textarea' ).each( function () {
        CKEDITOR.disableAutoInline = true;
        // { extraPlugins: 'autogrow', autoGrow_minHeight : 300, autoGrow_onStartup: true }
        CKEDITOR.inline( $( this ).attr( 'id' ), {} );
    } )

} );