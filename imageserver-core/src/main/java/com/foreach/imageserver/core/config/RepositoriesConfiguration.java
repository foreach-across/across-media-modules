package com.foreach.imageserver.core.config;

import com.foreach.imageserver.core.repositories.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
	public ContextRepository contextRepository() {
		return new ContextRepositoryImpl();
	}
}
