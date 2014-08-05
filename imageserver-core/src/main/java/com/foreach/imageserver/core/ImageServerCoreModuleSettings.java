package com.foreach.imageserver.core;

import org.springframework.core.env.Environment;

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
}
