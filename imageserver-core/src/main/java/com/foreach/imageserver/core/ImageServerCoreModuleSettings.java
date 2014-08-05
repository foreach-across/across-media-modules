package com.foreach.imageserver.core;

import org.springframework.core.env.Environment;

import java.io.File;

/**
 * @author Arne Vandamme
 */
public class ImageServerCoreModuleSettings
{
	/**
	 * Access token required for secured services.
	 * <p/>
	 * Type: string
	 */
	public static final String ACCESS_TOKEN = "imageServerCore.accessToken";

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
	public static final String IMAGEMAGICK_USE_GRAPHICSMAGIC =
			"imageServerCore.transformers.imageMagick.useGraphicsMagick";

	/**
	 * True if ghostscript is supported on the GraphicsMagick installation.
	 */
	public static final String IMAGEMAGICK_USE_GHOSTSCRIPT = "imageServerCore.transformers.imageMagick.useGhostScript";

	/**
	 * Path to the ImageMagick installation.
	 */
	public static final String IMAGEMAGICK_PATH = "";

	public static String getAccessToken( Environment environment ) {
		return environment.getProperty( ACCESS_TOKEN, "azerty" );
	}

	public static boolean shouldProvideStackTrace( Environment environment ) {
		return environment.getProperty( PROVIDE_STACKTRACE, Boolean.class, false );
	}

	public static int getMaxBrowserCacheSeconds( Environment environment ) {
		return environment.getProperty( MAX_BROWSER_CACHE_SECONDS, Integer.class, 60 );
	}

	public static String getImageNotFoundImageKey( Environment environment ) {
		return environment.getProperty( IMAGE_NOT_FOUND_IMAGEKEY );
	}

	public static File getImageStoreFolder( Environment environment ) {
		return environment.getRequiredProperty( IMAGE_STORE_FOLDER, File.class );
	}
}
