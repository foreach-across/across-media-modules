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
    var adminPrefix = AdminWebModule.rootPath;

    EntityModule.registerInitializer( function ( node ) {
        $( '[data-wcm-component-base-type=image]', node ).each( function () {
            var container = $( this );

            var selectImage = function ( imageId ) {
                $.get( adminPrefix + '/utils/buildImageUrl', {width: 188, height: 154, 'imageId': imageId} )
                        .done( function ( url ) {
                            container.find( '[data-wcm-component-property=image]' ).val( imageId );
                            container.find( 'img' ).attr( 'src', url );
                            container.find( '.image-thumbnail-container' ).removeClass( 'hidden' );
                            container.find( '.image-thumbnail-actions' ).removeClass( 'hidden' );
                            container.find( 'button[name=btn-select-image]' ).addClass( 'hidden' );
                        } );
            };

            $( 'a[data-wcm-image-action=delete]' ).on( 'click', function () {
                container.find( '[data-wcm-component-property=image]' ).val( '' );
                container.find( 'img' ).attr( 'src', '' );
                container.find( '.image-thumbnail-container' ).addClass( 'hidden' );
                container.find( '.image-thumbnail-actions' ).addClass( 'hidden' );
                container.find( 'button[name=btn-select-image]' ).removeClass( 'hidden' );
            } );

            $( 'button[name=btn-select-image], a[data-wcm-image-action=edit]', container ).on( 'click', function () {
                var imageObjectId = '';
                var dialog = bootbox.dialog(
                        {
                            message: '<p><i class="fa fa-spin fa-spinner"></i> Loading...</p>',
                            size: 'large',
                            className: 'image-selector',
                            onEscape: true,
                            buttons: {
                                confirm: {
                                    label: 'Use selected image',
                                    className: 'btn-primary',
                                    callback: function () {
                                        selectImage( imageObjectId );
                                    }
                                },
                                cancel: {
                                    label: 'Cancel',
                                    className: 'btn-link'
                                }
                            }
                        }
                );

                var ajaxify = function ( node, baseUrl ) {
                    dialog.find( '.modal-footer' ).hide();
                    dialog.find( '.modal-footer [data-bb-handler="confirm"]' ).addClass( 'disabled' );

                    if ( $( 'a[data-wcm-image-id]' ).length ) {
                        dialog.find( '.modal-footer' ).show();
                    }

                    $( 'a', node ).on( 'click', function ( e ) {
                        var link = $( this );
                        e.preventDefault();

                        if ( link.attr( 'data-wcm-image-id' ) ) {
                            dialog.find( '.image-thumbnail-container' ).removeClass( 'selected' );

                            imageObjectId = link.attr( 'data-wcm-image-id' );
                            link.parent().addClass( 'selected' );
                            dialog.find( '.modal-footer [data-bb-handler="confirm"]' ).removeClass( 'disabled' );

                        }
                        else {
                            get( link.attr( 'href' ) );
                        }
                    } );

                    $( 'form', node ).on( 'submit', function ( e ) {
                        e.preventDefault();

                        var useFormData = ("" + $( this ).attr( 'enctype' )).toLowerCase().indexOf( 'form-data' ) > 0;

                        var formConfig = {};

                        if ( useFormData ) {
                            var data = new FormData( $( this )[0] );

                            formConfig = {
                                type: $( this ).attr( 'method' ),
                                url: baseUrl,
                                data: data,
                                processData: false,
                                contentType: false
                            }
                        }
                        else {
                            formConfig = {
                                type: $( this ).attr( 'method' ),
                                url: baseUrl,
                                data: $( this ).serialize()
                            }
                        }

                        $.ajax( formConfig ).done(
                                function ( data ) {
                                    var body = dialog.find( '.bootbox-body' );
                                    body.html( data );
                                    ajaxify( body, baseUrl );
                                }
                        );
                    } );
                };

                var get = function ( url ) {
                    var baseUrl = url + '?_partial=content';
                    $.get( baseUrl, function ( data ) {
                        var body = dialog.find( '.bootbox-body' );
                        body.html( data );
                        ajaxify( body, baseUrl );
                    } );
                };

                get( adminPrefix + '/entities/webCmsImage' );
            } )
        } );
    } );
})( jQuery );