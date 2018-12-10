package com.foreach.imageserver.core.config;

import lombok.Data;

import java.io.File;

@Data
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
