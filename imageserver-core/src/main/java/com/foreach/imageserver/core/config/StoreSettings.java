package com.foreach.imageserver.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties("image-server-core.store")
@Getter
@Setter
public class StoreSettings
{
	/**
	 * Root location of the image store.
	 */
	private File folder;

	/**
	 * Permissions to set on created folders.
	 */
	private String folderPermissions = "";

	/**
	 * Permissions to set on created files.
	 */
	private String filePermissions = "";
}
