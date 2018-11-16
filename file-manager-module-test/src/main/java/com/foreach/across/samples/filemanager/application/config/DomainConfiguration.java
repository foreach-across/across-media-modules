package com.foreach.across.samples.filemanager.application.config;

import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.filemanager.business.reference.FileReference;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceRepository;
import com.foreach.across.modules.filemanager.business.reference.FileReferenceService;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.across.samples.filemanager.application.domain.Car;
import com.foreach.across.samples.filemanager.application.domain.FileReferenceId;
import com.foreach.across.samples.filemanager.application.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * @author Steven Gentens
 * @since 1.3.0
 */
@Configuration
@EnableAcrossJpaRepositories(basePackageClasses = Car.class)
@RequiredArgsConstructor
public class DomainConfiguration implements EntityConfigurer
{
	private final FileReferenceRepository fileReferenceRepository;
	private final FileReferenceService fileReferenceService;

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( FileReference.class ).show();

		entities.withType( User.class )
		        .properties( props -> props.property( "avatarId" ).hidden( true )
		                                   .and().property( "avatar" )
		                                   .propertyType( FileReference.class )
		                                   .displayName( "Avatar" )
		                                   .readable( true )
		                                   .writable( true )
		                                   .hidden( false )
		                                   .controller( c -> c.withTarget( User.class, FileReference.class )
		                                                      .valueFetcher( user -> user.getAvatarId() != null ? fileReferenceRepository
				                                                      .findById( user.getAvatarId().getFileReferenceId() ).orElse( null ) : null )
		                                                      .applyValueConsumer(
				                                                      ( user, fileReference ) -> {
					                                                      if ( fileReference.getNewValue() != null && !fileReference.isDeleted() ) {
						                                                      user.setAvatarId( new FileReferenceId( fileReference.getNewValue().getId() ) );
					                                                      }
					                                                      else {
						                                                      user.setAvatarId( null );
					                                                      }
				                                                      } )
		                                   )
		                                   .attribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART )
		        );

		// migrate the files attached under "other" to a different repository
		entities.withType( Car.class )
		        .properties(
				        props -> props
						        .property( "other[]" )
						        .controller( ctl -> ctl
								        .withBindingContext( FileReference.class )
								        .saveConsumer(
										        ( ctx, fr ) -> fileReferenceService
												        .changeFileRepository( fr.getNewValue(), "permanent", true )
								        )
						        )
		        );
	}

}
