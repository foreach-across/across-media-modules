/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.domain.image.connector;

import com.foreach.across.modules.webcms.domain.image.WebCmsImage;

/**
 * General API for storing {@link WebCmsImage} data (the physical files) in an external repository.
 * This API provides all basic methods that the administration UI needs to upload images, delete images,
 * and building a simple URL to an image.
 *
 * @author Arne Vandamme
 * @see CloudinaryWebCmsImageConnector
 * @see ImageServerWebCmsImageConnector
 * @since 0.0.2
 */
public interface WebCmsImageConnector
{
	int ORIGINAL_WIDTH = 0;
	int ORIGINAL_HEIGHT = 0;

	/**
	 * Associates physical image data with a {@link WebCmsImage}.
	 * Usually uploads the physical file to some kind of remote repository, and stores
	 * the returned reference using {@link WebCmsImage#setExternalId(String)}.
	 *
	 * @param image record
	 * @param data  actual image file
	 * @return true if saving was successful
	 */
	boolean saveImageData( WebCmsImage image, byte[] data );

	/**
	 * Builds a URL to render the given image.  The box width and height parameters
	 * indicate a box in which the image is supposed to fit.
	 * <p/>
	 * This method is expected to always return images according to their original aspect ratio.
	 *
	 * @param image     to render
	 * @param boxWidth  width of the box the image should fit in
	 * @param boxHeight height of the box the image should fit in
	 * @return url to the image
	 */
	String buildImageUrl( WebCmsImage image, int boxWidth, int boxHeight );

	/**
	 * Removes the physical data associated with a {@link WebCmsImage}.
	 * Usually calls the remote repository to delete the actual image file.
	 *
	 * @param image to remove the physical data from
	 * @return true if data has been deleted
	 */
	boolean deleteImageData( WebCmsImage image );
}
