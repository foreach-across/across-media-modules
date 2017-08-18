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
        $( '[data-wcm-component-id]' ).each(
                function () {
                    var component = $( this );
                    var firstLink = component.find( '> .pull-right > ul.wcm-component-tabs a[data-wcm-component-tab]' ).first();

                    component.find( ':input' ).change( function () {
                        $( this ).closest( 'form' ).data( 'changed', true );
                    } );

                    var selectTab = function ( link ) {
                        var tab = link.data( 'wcm-component-tab' );

                        component.find( '> .wcm-component-tab-pane' ).removeClass( 'active' );
                        component.find( '> .pull-right > ul.wcm-component-tabs li' ).removeClass( 'active' );

                        link.closest( 'li' ).addClass( 'active' );
                        var tabPane = link.attr( 'href' );
                        $( tabPane )
                                .addClass( 'active' )
                                .find( '[data-wcm-component-refresh]' )
                                .trigger( 'wcm:componentRefresh' );
                    };

                    selectTab( firstLink );

                    window.onbeforeunload = function ( e ) {
                        var form = $( '#btn-save' ).closest( 'form' );
                        if ( form.data( 'changed' ) === true ) {
                            return "You have unsaved changes, are you sure you want to navigate away from this page?"
                        }
                    };

                    $( '#btn-save' ).closest( 'form' ).submit( function () {
                        $( '#btn-save' ).closest( 'form' ).data( 'changed', false );
                        return true;
                    } );

                    component.find( '> .wcm-sortable-component' )
                            .sortable( {
                                           items: '.wcm-component-form-group',
                                           axis: 'y',
                                           update: function ( event, ui ) {
                                               component
                                                       .find( '.wcm-linked-component input[name$=sortIndex]' )
                                                       .each( function ( index ) {
                                                           $( this ).val( index + 1 );
                                                       } );
                                               $( this ).closest( 'form' ).data( 'changed', true );
                                           }
                                       } )
                            .disableSelection();

                    component.find( '> .pull-right > ul.wcm-component-tabs a' )
                            .each( function () {
                                var link = $( this );
                                var tab = link.data( 'wcm-component-tab' );
                                if ( tab ) {
                                    if ( $( link.attr( 'href' ) ).find( '.form-group.has-error' ).length > 0 ) {
                                        link.closest( 'li' ).addClass( 'has-error' );
                                        selectTab( link );
                                    }
                                }
                            } )
                            .on( 'click', function ( e ) {
                                var tab = $( this ).data( 'wcm-component-tab' );

                                if ( tab ) {
                                    selectTab( $( this ).closest( 'li' ).hasClass( 'active' ) ? firstLink : $( this ) );
                                    e.preventDefault();
                                }
                                else if ( !shouldNavigateAway( $( this ) ) ) {
                                    e.preventDefault();
                                }
                            } );
                }
        );

    } );
})( jQuery );