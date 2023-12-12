package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;

/**
 * API for building a secure hash of image request parameters.
 * When the server is not operating in strict mode and both server and client have the same
 * {@link ImageRequestHashBuilder} configured, all requests containing a valid hash for their
 * parameters will be accepted.  Whereas otherwise only requests for resolutions that are registered
 * server-side will be accepted.
 *
 * @author Arne Vandamme
 * @see Md5ImageRequestHashBuilder
 */
public interface ImageRequestHashBuilder
{
	/**
	 * Calculate a security hash of the different parameters.
	 * Result can be empty if no hash could be calculated.
	 *
	 * @param context         of the image
	 * @param ratio           can be null
	 * @param imageResolution can be null
	 * @param imageVariant    requested - can be null
	 * @param size            can be empty
	 * @return calculated hash
	 */
	String calculateHash( String context,
	                      String ratio,
	                      ImageResolutionDto imageResolution,
	                      ImageVariantDto imageVariant,
	                      String... size );

	/**
	 * Create a default {@link Md5ImageRequestHashBuilder} implementation using
	 * a base token to be added to the parameters when calculating the hash.
	 *
	 * @param hashToken token to use for the MD5 hash
	 * @return builder
	 */
	static ImageRequestHashBuilder md5( String hashToken ) {
		return new Md5ImageRequestHashBuilder( hashToken );
	}
}
