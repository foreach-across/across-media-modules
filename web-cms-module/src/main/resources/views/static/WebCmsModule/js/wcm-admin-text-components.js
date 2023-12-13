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
/**
 * Central registry for component profiles.
 * A profile is attached to a 'componentType' and has a unique name.
 * A profile is expected to be a JS object, usually a dictionary.
 *
 * It can optionally specify a '_parentProfile' holding the name of the parent
 * that should be merged into this profile.  Properties of the parent will be
 * overridden by the child.
 *
 * How a profile is used is entirely up to the actual component implementation,
 * this registry simply provides access to named configuration objects and the merging between parent-child.
 */
var WebCmsComponentProfileRegistry = (function ( $ ) {
    return {
        componentTypes: {},

        /**
         * Registers a new profile for a component type.
         *
         * @param componentType name of the component type
         * @param profileName name of the profile
         * @param profile dictionary object
         */
        registerProfileForComponentType: function ( componentType, profileName, profile ) {
            if ( !(componentType in this.componentTypes) ) {
                this.componentTypes[componentType] = {};
            }

            this.componentTypes[componentType][profileName] = profile;
        },

        /**
         * Get a named profile for a particular component type.
         *
         * @param componentType to get the profile for
         * @param profileName name of the profile
         * @param forTemplate true if no warnings should be logged (mainly for internal use)
         * @returns profile or null if none found
         */
        getProfile: function ( componentType, profileName, forTemplate ) {
            var actualProfileName = profileName ? profileName : 'default';

            var profiles = this.componentTypes[componentType];
            if ( profiles != null ) {
                var profile = profiles[actualProfileName];

                if ( profile != null ) {
                    if ( '_parentProfile' in profile ) {
                        var parentProfile = this.getProfile( componentType, profile['_parentProfile'], true );
                        if ( parentProfile != null ) {
                            var p = $.extend( {}, parentProfile );
                            p = $.extend( p, profile );
                            delete p['_parentProfile'];
                            return p;
                        }
                    }

                    var value = $.extend( {}, profile );
                    delete value['_parentProfile'];
                    return value;
                }
                else if ( !forTemplate ) {
                    console.warn( "No profile named '" + actualProfileName + "' for component type '" + componentType + "'" );
                }
            }
            else if ( !forTemplate ) {
                console.warn( "No profiles registered for component type '" + componentType + "'" );
            }

            return null;
        }

    };
})( jQuery );

(function ( $ ) {
    /**
     * Register base TinyMCE (rich-text) profile that configures technical settings.
     */
    WebCmsComponentProfileRegistry.registerProfileForComponentType( 'rich-text', '_base', {
        branding: false,
        relative_urls: false,
        remove_script_host: true,
        // todo: document_base_url: "http://localhost:8080",
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
        content_css: [
            AcrossWebModule.staticPath + '/WebCmsModule/css/wcm-tinymce-content.css'
        ],
        setup: function ( theEditor ) {
            theEditor.on( 'focus', function () {
                $( this.contentAreaContainer.parentElement )
                        .toggleClass( 'wcm-mce-toolbar-hidden', false )
                        .find( "div.mce-toolbar-grp" ).unstick().sticky( {topSpacing: 60} );
            } );
            theEditor.on( 'blur', function ( e ) {
                theEditor.selection.collapse();
                var parent = $( this.contentAreaContainer.parentElement );
                setTimeout( function () {
                    parent.toggleClass( 'wcm-mce-toolbar-hidden', true );
                }, 250 );
            } );
            theEditor.on( "init", function () {
                $( this.contentAreaContainer.parentElement )
                        .toggleClass( 'wcm-mce-toolbar-hidden', true )
                        .find( "div.mce-toolbar-grp" ).unstick();
            } );

            theEditor.on( "change", function ( cm ) {
                $( this.contentAreaContainer.parentElement ).closest( 'form' ).data( 'changed', true );
            } );
        }
    } );

    /**
     * Register default profile for TinyMCE editors.
     */
    WebCmsComponentProfileRegistry.registerProfileForComponentType( 'rich-text', 'default', {
        _parentProfile: '_base',
        plugins: 'autoresize image contextmenu table hr pagebreak code codesample noneditable lists advlist link paste textcolor charmap anchor',
        toolbar1: 'undo redo | insert | styleselect | bold italic forecolor backcolor ' +
        '| outdent indent | bullist numlist table | link image codesample | pastetext',
        insert_button_items: 'image link inserttable | pagebreak hr anchor charmap',
        autoresize_bottom_margin: 0,
        statusbar: false,
        menubar: false,
        object_resizing: 'img',
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
        ]
    } );

    /**
     * Basic rich-text profile is the same as default.
     */
    WebCmsComponentProfileRegistry.registerProfileForComponentType( 'rich-text', 'rich-text', {_parentProfile: 'default'} );

    /**
     * Register default markup profile.
     */
    WebCmsComponentProfileRegistry.registerProfileForComponentType( 'markup', 'default', {
        _parentProfile: '_base', lineNumbers: true, mode: 'htmlmixed', viewportMargin: Infinity
    } );

    /**
     * Initialize text components.
     * Uses the WebCmsComponentProfileRegistry where the value of 'data-wcm-markup-type' attribute is the component type.
     */
    EntityModule.registerInitializer( function ( node ) {
        $( '[data-wcm-markup-type=rich-text]', node ).each( function () {
            var id = '[id="' + $( this ).attr( 'id' ) + '"]';
            var profile = WebCmsComponentProfileRegistry.getProfile( 'rich-text', $( this ).data( 'wcm-profile' ) );
            profile['selector'] = id;

            tinymce.init( profile );
        } );

        $( '[data-wcm-markup-type=markup]', node ).each( function () {
            var profile = WebCmsComponentProfileRegistry.getProfile( 'markup', $( this ).data( 'wcm-profile' ) );
            var cm = CodeMirror.fromTextArea( $( this )[0], profile );

            cm.on( "change", function ( cm ) {
                $( cm.getTextArea().closest( 'form' ) ).data( 'changed', true );
            } );

            $( this ).on( 'wcm:componentRefresh',
                          function () {
                              cm.refresh();
                          } )
                    .attr( 'data-wcm-component-refresh', 'true' );

            if ( profile._configurationCallback ) {
                profile._configurationCallback( cm, $( this ) );
            }
        } );
    } );
})
( jQuery );