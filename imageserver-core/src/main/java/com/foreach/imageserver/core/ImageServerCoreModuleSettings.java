package com.foreach.imageserver.core;

/**
 * @author Arne Vandamme
 */
public interface ImageServerCoreModuleSettings
{
	/**
	 * Base path for all controllers.
	 */
	String ROOT_PATH = "imageServerCore.rootPath";

	/**
	 * URL for this ImageServer instance.  In case a local client will be created, this
	 * will be the base url for requesting images.
	 */
	String IMAGE_SERVER_URL = "imageServerCore.imageServerUrl";

	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	String ACCESS_TOKEN = "imageServerCore.accessToken";

	/**
	 * Should a {@link com.foreach.imageserver.core.client.LocalImageServerClient} instance be created
	 * for this ImageServer.  If true the instance will be created and exposed so dependant modules can use it.
	 * <p/>
	 * Type: boolean, default: false
	 */
	String CREATE_LOCAL_CLIENT = "imageServerCore.createLocalClient";

	/**
	 * Should exception stacktrace be returned to the caller.
	 * <p/>
	 * Type: boolean
	 */
	String PROVIDE_STACKTRACE = "imageServerCore.streaming.provideStackTrace";

	/**
	 * Number of seconds a browser (or proxy) is allowed to cache the image returned.
	 */
	String MAX_BROWSER_CACHE_SECONDS = "imageServerCore.streaming.maxBrowserCacheSeconds";

	/**
	 * Image server key for the image that should be returned in case the requested image was not found.
	 */
	String IMAGE_NOT_FOUND_IMAGEKEY = "imageServerCore.streaming.imageNotFoundKey";

	/**
	 * Root location of the image store.
	 */
	String IMAGE_STORE_FOLDER = "imageServerCore.store.folder";

	/**
	 * Permissions to set on created folders.
	 */
	String IMAGE_STORE_FOLDER_PERMISSIONS = "imageServerCore.store.folderPermissions";

	/**
	 * Permissions to set on created files.
	 */
	String IMAGE_STORE_FILE_PERMISSIONS = "imageServerCore.store.filePermissions";

	/**
	 * Number of concurrent transformations that can be busy.
	 * <p/>
	 * Type: int, default 10
	 */
	String TRANSFORMERS_CONCURRENT_LIMIT = "imageServerCore.transformers.concurrentLimit";

	/**
	 * True if image magick transformer should be created.
	 */
	String IMAGEMAGICK_ENABLED = "imageServerCore.transformers.imageMagick.enabled";

	/**
	 * Priority for the ImageMagick transformer.
	 */
	String IMAGEMAGICK_PRIORITY = "imageServerCore.transformers.imageMagick.priority";

	/**
	 * True if GraphicsMagick should be used instead of regular ImageMagick.
	 */
	String IMAGEMAGICK_USE_GRAPHICSMAGICK =
			"imageServerCore.transformers.imageMagick.useGraphicsMagick";

	/**
	 * True if ghostscript is supported on the GraphicsMagick installation.
	 */
	String IMAGEMAGICK_USE_GHOSTSCRIPT = "imageServerCore.transformers.imageMagick.useGhostScript";

	/**
	 * Path to the ImageMagick installation.
	 */
	String IMAGEMAGICK_PATH = "imageServerCore.transformers.imageMagick.path";
}
