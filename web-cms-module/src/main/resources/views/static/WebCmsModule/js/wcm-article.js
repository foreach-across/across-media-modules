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
        var container = node ? node : $( document );
        if ( container.find( '[id="entity.articleType"] option' ).length == 1 ) {
            container.find( '[id="entity.articleType"]' ).attr( 'readonly', 'readonly' );
        }
    } );

    $( document ).ready( function () {
        var loadArticleTypes = function ( publicationSelect ) {
            var selected = publicationSelect.val();

            $.get( window.url, $.param( {'_partial': '::articleType', 'entity.publication': selected} ) )
                    .done( function ( data ) {
                        $( '[id="entity.articleType"]' ).replaceWith( data );
                        EntityModule.initializeFormElements( $( '[id="entity.articleType"]' ).parent() );
                    } );
        };

        $( "[id='entity.publication']" )
                .on( 'change', function ( e ) {
                    loadArticleTypes( $( this ) );
                } )
                .each( function () {
                    if ( $( this ).find( 'option' ).length ) {
                        loadArticleTypes( $( this ) );
                    }
                } )
    } );

})( jQuery );
