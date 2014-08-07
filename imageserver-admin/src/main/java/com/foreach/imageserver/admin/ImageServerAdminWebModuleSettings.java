package com.foreach.imageserver.admin;

/**
 * @author Arne Vandamme
 */
public interface ImageServerAdminWebModuleSettings
{
	/**
	 * URL or base path for the ImageServer instance this admin should manage.
	 */
	String IMAGE_SERVER_URL = "imageServerAdmin.imageServerUrl";

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	String ACCESS_TOKEN = "imageServerAdmin.accessToken";
}
