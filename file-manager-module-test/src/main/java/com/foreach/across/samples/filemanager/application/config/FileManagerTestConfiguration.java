package com.foreach.across.samples.filemanager.application.config;

import com.foreach.across.modules.bootstrapui.elements.FormViewElement;
import com.foreach.across.modules.entity.EntityAttributes;
import com.foreach.across.modules.entity.config.EntityConfigurer;
import com.foreach.across.modules.entity.config.builders.EntitiesConfigurationBuilder;
import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
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

	@Override
	public void configure( EntitiesConfigurationBuilder entities ) {
		entities.withType( Car.class )
		        .createOrUpdateFormView(
				        fvb -> fvb.properties(
						        props -> props.property( "manual" )
						                      .attribute( EntityAttributes.FORM_ENCTYPE, FormViewElement.ENCTYPE_MULTIPART )
				        )
		        );
	}

}
