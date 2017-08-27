package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageAspectRatioDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;

import java.util.function.BooleanSupplier;

/**
 * @author Arne Vandamme
 */
public class ViewImageRequest extends ImageRequest
{
	private ImageResolutionDto imageResolutionDto;
	private ImageModificationDto imageModificationDto;
	private ImageVariantDto imageVariantDto;
	private ImageAspectRatioDto imageAspectRatioDto;
	private byte[] imageData;
	private Boolean securityCheckPassed;
	private BooleanSupplier securityCheckCallback;

	public ImageResolutionDto getImageResolutionDto() {
		return imageResolutionDto;
	}

	public void setImageResolutionDto( ImageResolutionDto imageResolutionDto ) {
		this.imageResolutionDto = imageResolutionDto;
	}

	public ImageVariantDto getImageVariantDto() {
		return imageVariantDto;
	}

	public void setImageVariantDto( ImageVariantDto imageVariantDto ) {
		this.imageVariantDto = imageVariantDto;
	}

	public ImageModificationDto getImageModificationDto() {
		return imageModificationDto;
	}

	public void setImageModificationDto( ImageModificationDto imageModificationDto ) {
		this.imageModificationDto = imageModificationDto;
	}

	public ImageAspectRatioDto getImageAspectRatioDto() {
		return imageAspectRatioDto;
	}

	public void setImageAspectRatioDto( ImageAspectRatioDto imageAspectRatioDto ) {
		this.imageAspectRatioDto = imageAspectRatioDto;
	}

	public byte[] getImageData() {
		return imageData;
	}

	public void setImageData( byte[] imageData ) {
		this.imageData = imageData;
	}

	/**
	 * Set the callback method to be executed the first time {@link #isValidCustomRequest()} is called.
	 *
	 * @param securityCheckCallback callback method
	 */
	public void setSecurityCheckCallback( BooleanSupplier securityCheckCallback ) {
		this.securityCheckCallback = securityCheckCallback;
	}

	/**
	 * Checks if this request has passed the security callback set by {@link #setSecurityCheckCallback(BooleanSupplier)}.
	 * Even if the request does not match any registered request, it would still be considered valid if strict mode
	 * is not active.
	 *
	 * @return true if this can be considered a valid custom request
	 */
	public boolean isValidCustomRequest() {
		if ( securityCheckPassed == null ) {
			securityCheckPassed = securityCheckCallback != null && securityCheckCallback.getAsBoolean();
		}

		return securityCheckPassed;
	}
}
