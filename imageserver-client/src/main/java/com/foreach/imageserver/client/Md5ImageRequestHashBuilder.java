package com.foreach.imageserver.client;

import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.DigestUtils;

/**
 * Default implementation of {@link ImageRequestHashBuilder} that calculates a uses an MD5
 * hash of all parameters including an additional custom hash token.
 *
 * @author Arne Vandamme
 */
public class Md5ImageRequestHashBuilder implements ImageRequestHashBuilder
{
	private final String hashToken;

	/**
	 * Create a new builder instance for creating MD5 hashes of all paremeters including an
	 * additional specified token.  If the server uses the same token it should be able to
	 * verify the generated hash codes.
	 *
	 * @param hashToken that will be added to the parameters when building the hash
	 */
	public Md5ImageRequestHashBuilder( String hashToken ) {
		Assert.notNull( hashToken );
		this.hashToken = hashToken;
	}

	@Override
	public String calculateHash( String context,
	                             String ratio,
	                             ImageResolutionDto imageResolution,
	                             ImageVariantDto imageVariant,
	                             String... size ) {
		StringBuilder buffer = new StringBuilder( hashToken )
				.append( context )
				.append( StringUtils.defaultString( ratio ) )
				.append( StringUtils.join( size ) );

		if ( imageResolution != null ) {
			buffer.append( "res" ).append( imageResolution.getWidth() ).append( imageResolution.getHeight() );
		}
		else {
			buffer.append( "res" );
		}

		if ( imageVariant != null ) {
			buffer.append( "var" );
			if ( imageVariant.getImageType() != null ) {
				buffer.append( imageVariant.getImageType() );
			}
			if ( imageVariant.getBoundaries() != null ) {
				buffer.append( imageVariant.getBoundaries().getWidth() ).
						append( imageVariant.getBoundaries().getHeight() );
			}
		}
		else {
			buffer.append( "var" );
		}

		return DigestUtils.md5DigestAsHex( buffer.toString().getBytes() );
	}
}
