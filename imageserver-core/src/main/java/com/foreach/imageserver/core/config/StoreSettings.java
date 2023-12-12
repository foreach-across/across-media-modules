package com.foreach.imageserver.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@Data
@ConfigurationProperties("image-server-core.store")
public class StoreSettings
{
	/**
	 * Root location of the image store.
	 */
	private File folder;
}
