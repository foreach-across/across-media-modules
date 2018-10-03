package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.bootstrapui.elements.builder.FileUploadFormElementBuilder;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.bind.EntityPropertyControlName;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyHandlingType;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.hibernate.business.IdBasedEntity;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders.*;
import static com.foreach.across.modules.entity.bind.EntityPropertyControlName.forProperty;
import static com.foreach.across.modules.entity.views.util.EntityViewElementUtils.currentPropertyDescriptor;

/**
 * Creates a file upload {@link com.foreach.across.modules.web.ui.ViewElement} for {@link FileReference} properties.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
@ConditionalOnBootstrapUI
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, EntityModule.NAME })
public class FileReferenceControlViewElementBuilder extends ViewElementBuilderSupport
{
	@Override
	protected void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.add( new WebResource( WebResource.JAVASCRIPT_PAGE_END, "file-reference-control",
		                                          "/static/fileManagerModule/js/file-upload.js",
		                                          WebResource.VIEWS ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElementBuilder wrapper = div();
		EntityPropertyDescriptor descriptor = currentPropertyDescriptor( builderContext );
		EntityPropertyControlName.ForProperty controlName = forProperty( descriptor, builderContext );

		Object value = EntityViewElementUtils.currentPropertyValue( builderContext );
		boolean isForMultiple = descriptor.getPropertyTypeDescriptor().isCollection();
		if ( isForMultiple ) {
			if ( value != null ) {
				addMultipleSelectedElements( wrapper, controlName, (List<FileReference>) value, builderContext );
			}
		}
		else {
			if ( value != null ) {
				addSingleSelectedElement( wrapper, (FileReference) value, builderContext );
			}
		}

		FileUploadFormElementBuilder fileUploadBuilder = file().css( "js-file-control" );
		if ( !isForMultiple ) {
			fileUploadBuilder.controlName( controlName.forHandlingType( EntityPropertyHandlingType.forProperty( descriptor ) ).toString() );
			if ( value != null ) {
				fileUploadBuilder.attribute( "data-id", ( (IdBasedEntity) value ).getId() );
			}
		}

		if ( isForMultiple ) {
			fileUploadBuilder.attribute( "data-multiple", "true" )
			                 .controlName( controlName.asCollectionItem().withBinderItemKey( 0 ).asBinderItem().withValue().toString() );
		}

		wrapper.addFirst( fileUploadBuilder );
		wrapper.addFirst( getTemplate() );
		return wrapper
				.css( "js-file-reference-control" )
				.build( builderContext );
	}

	private void addSingleSelectedElement( NodeViewElementBuilder wrapper, FileReference file, ViewElementBuilderContext builderContext ) {
		wrapper.add( selectedFileBuilder( file.getName(), builderContext.buildLink( FileReferenceUtils.getDownloadUrl( file ) ) )
		);
	}

	private void addMultipleSelectedElements( NodeViewElementBuilder wrapper,
	                                          EntityPropertyControlName.ForProperty controlName,
	                                          List<FileReference> files,
	                                          ViewElementBuilderContext builderContext ) {
		AtomicInteger integer = new AtomicInteger( 1 );
		files.stream().filter( Objects::nonNull ).forEach(
				file -> {
					int idx = integer.getAndIncrement();
					EntityPropertyControlName.ForProperty.BinderProperty.BinderPropertyValue binderPropertyValue =
							controlName.asCollectionItem().withBinderItemKey( idx ).asBinderItem().withValue();
					wrapper.add( selectedFileBuilder( file.getName(), builderContext.buildLink( FileReferenceUtils.getDownloadUrl( file ) ) )
							             .add( hidden().attribute( "data-item-idx", idx ).controlName( binderPropertyValue.toString() )
							                           .value( file.getId() ) ) );
				}

		);
	}

	private NodeViewElementBuilder getTemplate() {
		return node( "script" )
				.attribute( "type", "text/html" )
				.attribute( "data-role", "selected-item-template" )
				.add( selectedFileBuilder( "replaceByName", null ) );
	}

	private NodeViewElementBuilder selectedFileBuilder( String name, String url ) {
		return div().css( "file-reference-control-item" )
		            .add( StringUtils.isNotBlank( url )
				                  ? link().text( name ).url( url )
				                  : text( name ) )
		            .add( button().link().css( "remove-file" ).iconOnly( new GlyphIcon( GlyphIcon.REMOVE ) ) );
	}
}
