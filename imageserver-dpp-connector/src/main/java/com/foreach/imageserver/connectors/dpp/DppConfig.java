package com.foreach.imageserver.connectors.dpp;

import com.foreach.imageserver.services.repositories.ImageLookupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/config/${environment.type}/dpp-connector.properties")
public class DppConfig
{
	@Bean
	public ImageLookupRepository dioContentLookupRepository( @Value(
			"${repository.diocontent.http.url}") String serverUrl, @Value(
			"${repository.diocontent.login}") String login, @Value(
			"${repository.diocontent.password}") String password ) {
		return new DioContentLookupRepository( serverUrl, login, password );
	}

	@Bean
	public AssetConversionImageTransformer assetConversionImageTransformer( @Value(
			"${transformer.assetconversion.url") String serverUrl, @Value(
			"${transformer.assetconversion.enabled}") boolean enabled, @Value(
			"${transformer.assetconversion.priority}") int priority ) {
		return new AssetConversionImageTransformer( priority, enabled, serverUrl );
	}
}
