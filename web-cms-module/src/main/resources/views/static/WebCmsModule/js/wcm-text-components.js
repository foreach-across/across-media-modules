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
EntityModule.registerInitializer( function ( node ) {
    $( '[data-wcm-markup-type=rich-text]', node ).each( function () {
        CKEDITOR.disableAutoInline = true;
        // { extraPlugins: 'autogrow', autoGrow_minHeight : 300, autoGrow_onStartup: true }
        CKEDITOR.inline( $( this ).attr( 'id' ), {} );
    } );

    $( '[data-wcm-markup-type=markup]', node ).each( function () {
        CodeMirror.fromTextArea( $( this )[0], {lineNumbers: true, mode: 'htmlmixed'} );
    } );
} );