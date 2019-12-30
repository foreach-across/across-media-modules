package com.foreach.across.modules.filemanager.views.bootstrapui;

import com.foreach.across.modules.bootstrapui.elements.FormControlElement;
import com.foreach.across.modules.bootstrapui.elements.HiddenFormElement;
import com.foreach.across.modules.bootstrapui.elements.builder.FileUploadFormElementBuilder;
import com.foreach.across.modules.bootstrapui.elements.builder.ScriptViewElementBuilder;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.bind.EntityPropertyBinder;
import com.foreach.across.modules.entity.bind.EntityPropertyControlName;
import com.foreach.across.modules.entity.bind.ListEntityPropertyBinder;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyDescriptor;
import com.foreach.across.modules.entity.registry.properties.EntityPropertyHandlingType;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.resource.WebResourceRule;
import com.foreach.across.modules.web.ui.MutableViewElement;
import com.foreach.across.modules.web.ui.ViewElement;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.ViewElementBuilderSupport;
import com.foreach.across.modules.web.ui.elements.builder.NodeViewElementBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;
import static com.foreach.across.modules.entity.bind.EntityPropertyControlName.forProperty;
import static com.foreach.across.modules.entity.views.util.EntityViewElementUtils.currentPropertyBinder;
import static com.foreach.across.modules.entity.views.util.EntityViewElementUtils.currentPropertyDescriptor;
import static com.foreach.across.modules.filemanager.FileManagerModuleIcons.fileManagerIcons;
import static com.foreach.across.modules.web.ui.elements.HtmlViewElements.html;

/**
 * Creates a file upload {@link com.foreach.across.modules.web.ui.ViewElement} for {@link FileReference} properties.
 *
 * @author Steven Gentens
 * @since 1.3.0
 */
public class FileReferenceControlViewElementBuilder extends ViewElementBuilderSupport
{
	@Override
	protected void registerWebResources( WebResourceRegistry webResourceRegistry ) {
		webResourceRegistry.apply(
				WebResourceRule.add( WebResource.javascript( "@static:/FileManagerModule/js/file-upload.js" ) )
				               .withKey( "file-reference-control" )
				               .toBucket( WebResource.JAVASCRIPT_PAGE_END )
		);
	}

	@Override
	protected MutableViewElement createElement( ViewElementBuilderContext builderContext ) {
		NodeViewElementBuilder wrapper = html.builders.div();

		EntityPropertyBinder propertyBinder = currentPropertyBinder( builderContext );
		EntityPropertyDescriptor propertyDescriptor = currentPropertyDescriptor( builderContext );
		EntityPropertyControlName.ForProperty controlName = forProperty( propertyDescriptor, builderContext );

		boolean isForMultiple = propertyBinder instanceof ListEntityPropertyBinder;

		FileUploadFormElementBuilder fileUploadBuilder = bootstrap.builders.fileUpload().css( "js-file-control" );

		if ( isForMultiple ) {
			addMultipleSelectedElements( wrapper, controlName, (ListEntityPropertyBinder) propertyBinder, builderContext );

			wrapper.data( "multiple", true );

			fileUploadBuilder
					.data( "role", "file-upload-template" )
					.data( "control-name", controlName.asCollectionItem().withBinderItemKey( "{{key}}" ).asBinderItem().withValue().toString() );

			wrapper.add( boundIndicator( controlName ) )
			       .add( formGroupControl( controlName, EntityAttributes.isRequired( propertyDescriptor ) ) );
		}
		else {
			fileUploadBuilder.controlName( controlName.forHandlingType( EntityPropertyHandlingType.forProperty( propertyDescriptor ) ).toString() )
			                 .required( EntityAttributes.isRequired( propertyDescriptor ) );

			FileReference value = (FileReference) propertyBinder.getValue();

			if ( value != null ) {
				addSingleSelectedElement( wrapper, value, builderContext );
				fileUploadBuilder.data( "id", value.getId() );
			}
		}
		return wrapper
				.css( "js-file-reference-control" )
				.data( "next-item-id", System.currentTimeMillis() )
				.add( fileUploadBuilder )
				.add( selectedItemTemplate() )
				.build( builderContext );
	}

	private ViewElement formGroupControl( EntityPropertyControlName controlName, boolean required ) {
		HiddenFormElement hidden = new HiddenFormElement();
		hidden.setControlName( controlName.toString() );
		hidden.setDisabled( true );

		FormControlElement control = hidden.toFormControl();
		control.setRequired( required );

		return control;
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
					              EntityPropertyControlName.ForProperty.BinderProperty binderProperty = controlName.asCollectionItem()
					                                                                                               .withBinderItemKey( item.getItemKey() )
					                                                                                               .asBinderItem();

					              wrapper.add(
							              selectedFileBuilder( file.getName(), builderContext.buildLink( FileReferenceUtils.getDownloadUrl( file ) ) )
									              .add( bootstrap.builders.hidden().controlName( binderProperty.withValue().toString() ).value( file.getId() ) )
									              .add( bootstrap.builders.hidden().controlName( binderProperty.toSortIndex() ).value( item.getSortIndex() ) )
					              );
				              }

		              );
	}

	private ScriptViewElementBuilder selectedItemTemplate() {
		return bootstrap.builders.script()
		                         .type( MediaType.TEXT_HTML )
		                         .attribute( "data-role", "selected-item-template" )
		                         .add( selectedFileBuilder( "{{fileName}}", null ) );
	}

	private NodeViewElementBuilder selectedFileBuilder( String name, String url ) {
		return html.builders.div().css( "file-reference-control-item" )
		                    .add( StringUtils.isNotBlank( url )
				                          ? bootstrap.builders.link().text( name ).url( url )
				                          : html.builders.text( name ) )
		                    .add( bootstrap.builders.button().link().css( "remove-file" ).iconOnly( fileManagerIcons.removeFile() ) );
	}
}
