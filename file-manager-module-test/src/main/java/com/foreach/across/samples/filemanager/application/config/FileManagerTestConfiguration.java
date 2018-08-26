package com.foreach.across.samples.filemanager.application.config;

import com.foreach.across.modules.bootstrapui.elements.BootstrapUiBuilders;
import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.bootstrapui.elements.GlyphIcon;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.across.modules.web.resource.WebResource;
import com.foreach.across.modules.web.resource.WebResourceRegistry;
import com.foreach.across.modules.web.ui.ViewElementBuilderContext;
import com.foreach.across.modules.web.ui.elements.HtmlViewElement;
import com.foreach.across.modules.web.ui.elements.NodeViewElement;
import com.foreach.across.samples.filemanager.application.domain.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = Car.class)
@RequiredArgsConstructor
public class FileManagerTestConfiguration implements EntityConfigurer
{
	private final FileManager fileManager;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( Car.class )
		        .createOrUpdateFormView(
				        fvb -> fvb.properties(
						        props -> props.property( "manual" )
						                      .viewElementBuilder( ViewElementMode.CONTROL,
						                                           BootstrapUiBuilders.div()
						                                                              .css( "js-file-reference-control" )
						                                                              .add( BootstrapUiBuilders.file().controlName( "entity.manual" ) )
						                      )
						                      .viewElementPostProcessor( ViewElementMode.CONTROL, ( ( builderContext, viewElement ) -> {
							                      WebResourceRegistry webResourceRegistry = builderContext.getAttribute( WebResourceRegistry.class );
							                      webResourceRegistry.add( new WebResource( WebResource.JAVASCRIPT_PAGE_END, "file-reference-control",
							                                                                "/static/fileManagerModule/js/file-upload.js",
							                                                                WebResource.VIEWS ) );
							                      FileReference fileReference = EntityViewElementUtils.currentPropertyValue( builderContext,
							                                                                                                 FileReference.class );

							                      NodeViewElement div = (NodeViewElement) viewElement;
							                      if ( fileReference != null ) {
								                      ( (HtmlViewElement) div.getChildren().get( 0 ) ).addCssClass( "hidden" );
								                      div.addChild( createSelectedViewElement( builderContext, fileReference.getName() ) );
							                      }

							                      div.addFirstChild( BootstrapUiBuilders.node( "script" )
							                                                            .attribute( "type", "text/html" )
							                                                            .attribute( "data-role", "selected-item-template" )
							                                                            .add( createSelectedViewElement( builderContext, "replaceByName" ) )
							                                                            .build( builderContext ) );

						                      } ) )
						                      .attribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART )
				        )
		        );
	}

	private NodeViewElement createSelectedViewElement( ViewElementBuilderContext builderContext, String name ) {
		return BootstrapUiBuilders.div()
		                          .add( BootstrapUiBuilders.text( name )
		                                                   .build( builderContext ) )
		                          .add( BootstrapUiBuilders.button().link()
		                                                   .iconOnly( new GlyphIcon( GlyphIcon.REMOVE ) )
		                                                   .build( builderContext ) )
		                          .build( builderContext );
	}

}
