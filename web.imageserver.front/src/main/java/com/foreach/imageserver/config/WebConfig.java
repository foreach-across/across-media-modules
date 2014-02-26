package com.foreach.imageserver.config;

import com.foreach.imageserver.web.interceptors.GlobalVariableInterceptor;
import com.foreach.spring.logging.LogbackConfigurer;
import com.foreach.web.converter.EnumConverterFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.view.JstlView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.foreach.imageserver")
public class WebConfig extends WebMvcConfigurerAdapter
{
	@Override
	public void addInterceptors( InterceptorRegistry registry ) {
		registry.addInterceptor( globalVariableInterceptor() );
		registry.addInterceptor( cachingInterceptor() );
	}

	@Override
	public void addFormatters( FormatterRegistry registry ) {
		registry.addConverterFactory( new EnumConverterFactory() );
	}

	@Bean
	public LogbackConfigurer logbackConfigurer( @Value("${log.dir}") String logDir,
	                                            @Value("${log.config}") Resource baseConfig,
	                                            @Value("${log.config.extend}") Resource envConfig ) {
		return new LogbackConfigurer( logDir, baseConfig, envConfig );
	}

	@Bean
	public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer( @Value(
			"classpath:/config/${environment.type}/common.properties") Resource defaultProperties, @Value("file:${user.home}/dev-configs/imageserver.local.properties") Resource localProperties ) {
		PropertySourcesPlaceholderConfigurer propertySources = new PropertySourcesPlaceholderConfigurer();
		propertySources.setLocations( new Resource[] { defaultProperties, localProperties } );
		propertySources.setIgnoreResourceNotFound( true );
		propertySources.setIgnoreUnresolvablePlaceholders( true );

		return propertySources;
	}

	@Bean
	public GlobalVariableInterceptor globalVariableInterceptor() {
		return new GlobalVariableInterceptor();
	}

	@Bean
	public WebContentInterceptor cachingInterceptor() {
		WebContentInterceptor interceptor = new WebContentInterceptor();
		interceptor.setCacheSeconds( 0 );
		interceptor.setUseExpiresHeader( true );
		interceptor.setUseCacheControlHeader( true );
		interceptor.setUseCacheControlNoStore( true );

		return interceptor;
	}

	@Bean
	public UrlBasedViewResolver viewResolver() {
		UrlBasedViewResolver viewResolver = new UrlBasedViewResolver();
		viewResolver.setViewClass( JstlView.class );
		viewResolver.setPrefix( "/WEB-INF/jsp/" );
		viewResolver.setRedirectContextRelative( true );
		viewResolver.setSuffix( ".jsp" );
		return viewResolver;
	}
}
