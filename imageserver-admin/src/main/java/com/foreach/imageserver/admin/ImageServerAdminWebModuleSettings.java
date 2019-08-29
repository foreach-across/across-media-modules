package com.foreach.imageserver.admin;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("image-server-admin")
public class ImageServerAdminWebModuleSettings
{
	public static final String IMAGE_SERVER_URL = "imageServerAdmin.imageServerUrl";
	public static final String ACCESS_TOKEN = "imageServerAdmin.accessToken";

	/**
	 * URL or base path for the ImageServer instance this admin should manage.
	 */
	private String imageServerUrl;

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	private String accessToken;
}
