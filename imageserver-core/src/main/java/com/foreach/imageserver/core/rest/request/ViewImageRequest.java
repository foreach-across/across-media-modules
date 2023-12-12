package com.foreach.imageserver.core.rest.request;

import com.foreach.imageserver.dto.ImageAspectRatioDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import com.foreach.imageserver.dto.ImageVariantDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.function.BooleanSupplier;

/**
 * @author Arne Vandamme
 */
@Getter
@Setter
public class ViewImageRequest extends ImageRequest
{
	private ImageResolutionDto imageResolutionDto;
	private ImageModificationDto imageModificationDto;
	private ImageVariantDto imageVariantDto;
	private ImageAspectRatioDto imageAspectRatioDto;
	private byte[] imageData;
	private Boolean securityCheckPassed;
	/**
	 * -- SETTER --
	 * Set the callback method to be executed the first time {@link #isValidCustomRequest()} is called.
	 *
	 * @param securityCheckCallback callback method
	 */
	@Getter(AccessLevel.NONE)
	private BooleanSupplier securityCheckCallback;

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
