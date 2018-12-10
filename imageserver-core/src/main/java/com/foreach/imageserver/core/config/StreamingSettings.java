package com.foreach.imageserver.core.config;

import lombok.Data;

@Data
public class StreamingSettings
{
	/**
	 * Should exception stacktrace be returned to the caller.
	 * <p/>
	 * Type: boolean
	 */
	private Boolean provideStackTrace = false;

	/**
	 * Number of seconds a browser (or proxy) is allowed to cache the image returned.
	 */
	private Integer maxBrowserCacheSeconds = 60;

	/**
	 * String representation of the timespan that Akamai is allowed to cache the image returned.
	 */
	private String akamaiCacheMaxAge = "";

	/**
	 * Image server key for the image that should be returned in case the requested image was not found.
	 */
	private String imageNotFoundKey = "";
}
