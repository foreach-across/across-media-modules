package com.foreach.imageserver.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@Data
@ConfigurationProperties("imageServerCore.store")
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
