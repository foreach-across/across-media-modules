package com.foreach.imageserver.core.config;

import com.foreach.across.core.annotations.Exposed;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * Exposes a MultipartResolver to the partent context.
 *
 * @author Arne Vandamme
 */
@Configuration
public class MultipartResolverConfiguration
{
	@Exposed
	@Bean
	public MultipartResolver multipartResolver() {
		return new CommonsMultipartResolver();
	}
}
