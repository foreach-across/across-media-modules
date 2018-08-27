package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.bootstrapui.elements.builder.FileUploadFormElementBuilder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;

import static com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders.*;

/**
 * Creates a file upload {@link com.foreach.across.modules.web.ui.ViewElement} for {@link FileReference} properties.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileReferenceViewElementBuilder extends ViewElementBuilderSupport
{
	@Override
	protected void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.add( new WebResource( WebResource.JAVASCRIPT_PAGE_END, "file-reference-control",
		                                          "/static/fileManagerModule/js/file-upload.js",
		                                          WebResource.VIEWS ) );
	}

	@Override
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		FileReference fileReference = EntityViewElementUtils.currentPropertyValue( builderContext, FileReference.class );

		NodeViewElementBuilder wrapper = div();
		EntityPropertyDescriptor entityPropertyDescriptor = EntityViewElementUtils.currentPropertyDescriptor( builderContext );
		FileUploadFormElementBuilder fileUploadBuilder = file().controlName( "entity." + entityPropertyDescriptor.getName() );
		if ( fileReference != null ) {
			fileUploadBuilder.css( "hidden" );
			wrapper.add( selectedFileBuilder( fileReference.getName() ) );
		}
		wrapper.add( fileUploadBuilder );
		wrapper.addFirst( getTemplate() );
		return wrapper
				.css( "js-file-reference-control" )
				.build( builderContext );
	}

	private NodeViewElementBuilder getTemplate() {
		return node( "script" )
				.attribute( "type", "text/html" )
				.attribute( "data-role", "selected-item-template" )
				.add( selectedFileBuilder( "replaceByName" ) );
	}

	private NodeViewElementBuilder selectedFileBuilder( String name ) {
		return BootstrapUiBuilders.div()
		                          .add( BootstrapUiBuilders.text( name ) )
		                          .add( BootstrapUiBuilders.button().link()
		                                                   .iconOnly( new GlyphIcon( GlyphIcon.REMOVE ) ) );
	}
}
