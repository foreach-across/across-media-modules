package com.foreach.imageserver.core;

import com.foreach.across.core.AcrossModuleSettings;
import com.foreach.across.core.AcrossModuleSettingsRegistry;

/**
 * @author Arne Vandamme
 */
public class ImageServerCoreModuleSettings extends AcrossModuleSettings
{
	/**
	 * Base path for all controllers.
	 */
	public static final String ROOT_PATH = "imageServerCore.rootPath";

	/**
	 * URL for this ImageServer instance.  In case a local client will be created, this
	 * will be the base url for requesting images.
	 */
	public static final String IMAGE_SERVER_URL = "imageServerCore.imageServerUrl";

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	public static final String ACCESS_TOKEN = "imageServerCore.accessToken";

	/**
	 * Should a {@link com.foreach.imageserver.core.client.LocalImageServerClient} instance be created
	 * for this ImageServer.  If true the instance will be created and exposed so dependant modules can use it.
	 * <p/>
	 * Type: boolean, default: false
	 */
	public static final String CREATE_LOCAL_CLIENT = "imageServerCore.createLocalClient";

	/**
	 * Should exception stacktrace be returned to the caller.
	 * <p/>
	 * Type: boolean
	 */
	public static final String PROVIDE_STACKTRACE = "imageServerCore.streaming.provideStackTrace";

	/**
	 * Number of seconds a browser (or proxy) is allowed to cache the image returned.
	 */
	public static final String MAX_BROWSER_CACHE_SECONDS = "imageServerCore.streaming.maxBrowserCacheSeconds";

	/**
	 * String representation of the timespan that Akamai is allowed to cache the image returned.
	 */
	public static final String AKAMAI_CACHE_MAX_AGE = "imageServerCore.streaming.akamaiCacheMaxAge";

	/**
	 * Image server key for the image that should be returned in case the requested image was not found.
	 */
	public static final String IMAGE_NOT_FOUND_IMAGEKEY = "imageServerCore.streaming.imageNotFoundKey";

	/**
	 * Root location of the image store.
	 */
	public static final String IMAGE_STORE_FOLDER = "imageServerCore.store.folder";

	/**
	 * Permissions to set on created folders.
	 */
	public static final String IMAGE_STORE_FOLDER_PERMISSIONS = "imageServerCore.store.folderPermissions";

	/**
	 * Permissions to set on created files.
	 */
	public static final String IMAGE_STORE_FILE_PERMISSIONS = "imageServerCore.store.filePermissions";

	/**
	 * Number of concurrent transformations that can be busy.
	 * <p/>
	 * Type: int, default 10
	 */
	public static final String TRANSFORMERS_CONCURRENT_LIMIT = "imageServerCore.transformers.concurrentLimit";

	/**
	 * True if image magick transformer should be created.
	 */
	public static final String IMAGEMAGICK_ENABLED = "imageServerCore.transformers.imageMagick.enabled";

	/**
	 * Priority for the ImageMagick transformer.
	 */
	public static final String IMAGEMAGICK_PRIORITY = "imageServerCore.transformers.imageMagick.priority";

	/**
	 * True if GraphicsMagick should be used instead of regular ImageMagick.
	 */
	public static final String IMAGEMAGICK_USE_GRAPHICSMAGICK =
			"imageServerCore.transformers.imageMagick.useGraphicsMagick";

	/**
	 * True if ghostscript is supported on the GraphicsMagick installation.
	 */
	public static final String IMAGEMAGICK_USE_GHOSTSCRIPT = "imageServerCore.transformers.imageMagick.useGhostScript";

	/**
	 * Path to the ImageMagick installation.
	 */
	public static final String IMAGEMAGICK_PATH = "imageServerCore.transformers.imageMagick.path";

	@Override
	protected void registerSettings( AcrossModuleSettingsRegistry registry ) {

	}
}
