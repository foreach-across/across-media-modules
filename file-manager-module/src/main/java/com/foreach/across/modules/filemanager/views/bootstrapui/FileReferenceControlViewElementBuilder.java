package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.bootstrapui.elements.HiddenFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.FileUploadFormElementBuilder;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.bind.EntityPropertyBinder;
import com.foreach.across.modules.entity.bind.EntityPropertyControlName;
import com.foreach.across.modules.entity.bind.ListEntityPropertyBinder;
import com.foreach.across.modules.entity.conditionals.ConditionalOnBootstrapUI;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyHandlingType;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import org.apache.commons.lang3.StringUtils;

import static com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders.*;
import static com.foreach.across.modules.entity.bind.EntityPropertyControlName.forProperty;
import static com.foreach.across.modules.entity.views.util.EntityViewElementUtils.currentPropertyBinder;
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
		                                          "/static/FileManagerModule/js/file-upload.js",
		                                          WebResource.VIEWS ) );
	}

	@Override
	@SuppressWarnings("unchecked")
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElementBuilder wrapper = div();

		EntityPropertyBinder propertyBinder = currentPropertyBinder( builderContext );
		EntityPropertyDescriptor descriptor = currentPropertyDescriptor( builderContext );
		EntityPropertyControlName.ForProperty controlName = forProperty( descriptor, builderContext );

		boolean isForMultiple = propertyBinder instanceof ListEntityPropertyBinder;

		FileUploadFormElementBuilder fileUploadBuilder = file().css( "js-file-control" );

		if ( isForMultiple ) {
			addMultipleSelectedElements( wrapper, controlName, (ListEntityPropertyBinder) propertyBinder, builderContext );

			wrapper.data( "multiple", true );

			fileUploadBuilder.data( "role", "file-upload-template" )
			                 .data( "control-name", controlName.asCollectionItem().withBinderItemKey( "{{key}}" ).asBinderItem().withValue().toString() );

			wrapper.add( boundIndicator( controlName ) );
		}
		else {
			fileUploadBuilder.controlName( controlName.forHandlingType( EntityPropertyHandlingType.forProperty( descriptor ) ).toString() );

			FileReference value = (FileReference) propertyBinder.getValue();

			if ( value != null ) {
				addSingleSelectedElement( wrapper, value, builderContext );
				fileUploadBuilder.data( "id", value.getId() );
			}
		}
		return wrapper
				.css( "js-file-reference-control" )
				.add( fileUploadBuilder )
				.add( getTemplate() )
				.build( builderContext );
	}

	private ViewElement boundIndicator( EntityPropertyControlName.ForProperty controlName ) {
		HiddenFormElement hidden = new HiddenFormElement();
		hidden.setControlName( controlName.asBinderItem().toBound() );
		hidden.setValue( "1" );
		return hidden;
	}

	private void addSingleSelectedElement( NodeViewElementBuilder wrapper, FileReference file, ViewElementBuilderContext builderContext ) {
		wrapper.add( selectedFileBuilder( file.getName(), builderContext.buildLink( FileReferenceUtils.getDownloadUrl( file ) ) ) );
	}

	private void addMultipleSelectedElements( NodeViewElementBuilder wrapper,
	                                          EntityPropertyControlName.ForProperty controlName,
	                                          ListEntityPropertyBinder propertyBinder,
	                                          ViewElementBuilderContext builderContext ) {
		propertyBinder.getItemList()
		              .forEach(
				              item -> {
					              FileReference file = (FileReference) item.getValue();
					              EntityPropertyControlName.ForProperty.BinderProperty.BinderPropertyValue binderPropertyValue =
							              controlName.asCollectionItem().withBinderItemKey( item.getSortIndex() ).asBinderItem().withValue();

					              wrapper.add(
							              selectedFileBuilder( file.getName(), builderContext.buildLink( FileReferenceUtils.getDownloadUrl( file ) ) )
									              .add( hidden().attribute( "data-item-idx", item.getSortIndex() )
									                            .controlName( binderPropertyValue.toString() )
									                            .value( file.getId() )
									              )
					              );
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
