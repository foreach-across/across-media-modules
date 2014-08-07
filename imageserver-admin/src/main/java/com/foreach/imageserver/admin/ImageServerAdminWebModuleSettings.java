package com.foreach.imageserver.admin;

/**
 * @author Arne Vandamme
 */
public interface ImageServerAdminWebModuleSettings
{
	/**
	 * URL or base path for the ImageServer instance this admin should manage.
	 */
	public static final String IMAGE_SERVER_URL = "imageServerAdmin.imageServerUrl";

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	public static final String ACCESS_TOKEN = "imageServerAdmin.accessToken";
}
