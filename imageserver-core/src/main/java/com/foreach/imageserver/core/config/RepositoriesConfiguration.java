package com.foreach.imageserver.core.config;

import com.foreach.across.modules.hibernate.jpa.repositories.config.EnableAcrossJpaRepositories;
import com.foreach.imageserver.core.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableAcrossJpaRepositories(basePackageClasses = ImageRepository.class)
@Configuration
public class RepositoriesConfiguration
{
	@Bean
	public ImageRepository imageRepository() {
		return new ImageRepositoryImpl();
	}

	@Bean
	public ImageProfileRepository imageProfileRepository() {
		return new ImageProfileRepositoryImpl();
	}

	@Bean
	public ImageResolutionRepository imageResolutionRepository() {
		return new ImageResolutionRepositoryImpl();
	}

	@Bean
	public ImageModificationRepository imageModificationRepository() {
		return new ImageModificationRepositoryImpl();
	}

}
