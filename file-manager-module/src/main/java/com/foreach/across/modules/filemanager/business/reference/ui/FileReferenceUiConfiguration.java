package com.foreach.across.modules.filemanager.business.reference.ui;

import com.foreach.across.core.annotations.ConditionalOnAcrossModule;
import com.foreach.across.modules.bootstrapui.elements.StaticFormElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.entity.actions.FixedEntityAllowableActionsBuilder;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.entity.views.ViewElementMode;
import com.foreach.across.modules.entity.views.util.EntityViewElementUtils;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import com.foreach.across.modules.filemanager.utils.FileReferenceUtils;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.spring.security.actions.AllowableAction;
import com.foreach.across.modules.spring.security.actions.AllowableActionSet;
import com.foreach.across.modules.web.ui.ViewElementBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;

import java.util.Arrays;

import static com.foreach.across.modules.bootstrapui.ui.factories.BootstrapViewElements.bootstrap;

/**
 * Hides the default {@link FileReference} admin UI. But also configures the default views as readonly
 * (in case re-enabled) and sets the delete action to delete physical files as well. This can be useful
 * for development purposes.
 *
 * @author Arne Vandamme
 * @since 1.3.0
 */
@ConditionalOnAcrossModule(allOf = { AcrossHibernateJpaModule.NAME, EntityModule.NAME })
@RequiredArgsConstructor
@ConditionalOnBean(FileReferenceService.class)
@Configuration
class FileReferenceUiConfiguration implements EntityConfigurer
{
	private final FileReferenceService fileReferenceService;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.create()
		        .entityType( FileDescriptor.class, true )
		        .attribute( EntityAttributes.IS_EMBEDDED_OBJECT, false )
		        .hide();

		entities.withType( FileReference.class )
		        .hide()
		        .allowableActionsBuilder(
				        new FixedEntityAllowableActionsBuilder( new AllowableActionSet( Arrays.asList( AllowableAction.READ, AllowableAction.DELETE ) ) )
		        )
		        .entityModel(
				        model -> model.deleteMethod( fr -> fileReferenceService.delete( fr, true ) )
		        )
		        .properties(
				        props -> props.property( "name" )
				                      .viewElementBuilder( ViewElementMode.VALUE, fileReferenceDownloadLinkBuilder( false ) )
				                      .viewElementBuilder( ViewElementMode.LIST_VALUE, fileReferenceDownloadLinkBuilder( true ) )
		        )
		        .listView( lvb -> lvb.showProperties( "name", "fileSize", "mimeType", "lastModified" )
		                             .defaultSort( Sort.by( Sort.Direction.DESC, "lastModified" ) ) );
	}

	private ViewElementBuilder fileReferenceDownloadLinkBuilder( boolean forList ) {
		return builderContext -> {
			FileReference fileReference = EntityViewElementUtils.currentEntity( builderContext, FileReference.class );
			return bootstrap.builders.link()
			                         .url( FileReferenceUtils.getDownloadUrl( fileReference ) )
			                         .text( fileReference.getName() )
			                         .map( link -> {
				                         if ( !forList ) {
					                         StaticFormElement wrapper = new StaticFormElement();
					                         wrapper.addChild( link );
					                         return wrapper;
				                         }
				                         return link;
			                         } )
			                         .build( builderContext );

		};
	}
}

