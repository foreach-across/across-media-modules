package com.foreach.imageserver.test.standalone.module.config;

import com.foreach.across.core.annotations.Module;
import com.foreach.across.modules.web.mvc.PrefixingRequestMappingHandlerMapping;
import com.foreach.imageserver.core.ImageServerCoreModule;
import com.foreach.common.web.logging.RequestLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @author Arne Vandamme
 */
@Configuration
public class StandaloneConfiguration
{
	@Autowired
	@Module(ImageServerCoreModule.NAME)
	private PrefixingRequestMappingHandlerMapping handlerMapping;

	@Bean
	public RequestLogInterceptor requestLogInterceptor() {
		return new RequestLogInterceptor();
	}

	@PostConstruct
	public void registerInterceptor() {
		handlerMapping.addInterceptor( requestLogInterceptor() );
		handlerMapping.reload();
	}
}
