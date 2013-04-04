package com.foreach.imageserver.api.services;

import com.foreach.imageserver.api.business.UploadStatus;
import com.foreach.imageserver.api.models.ImageModel;
import com.foreach.imageserver.api.models.ImageServerUploadResult;

/**
 * An interface that allows uploading images and retrieving their urls from an image server.
 *
 */
public interface ImageServerConnection
{
    /**
     * Uploads an image to the image server.
     *
     * Returns an {@link ImageServerUploadResult} result that can be used to determine whether the image has been uploaded
     * successfully.
     *
     * @param image an {@link ImageModel} object that contains the data and information of the image to be uploaded
     * @return an {@link ImageServerUploadResult} object
     */
    ImageServerUploadResult uploadImage( ImageModel image );

    /**
     * Replaces an existing image on the image server
     *
     * Returns an {@link ImageServerUploadResult} result that can be used to determine whether the image has been replaced
     * successfully.
     *
     * @param imageId the image id as is given back in the {@link ImageServerUploadResult} object when uploading an image
     * @param image an {@link ImageModel} object that contains the data and information of the image to be replaced
     * @return an {@link ImageServerUploadResult} object
     */
	ImageServerUploadResult replaceImage( String imageId, ImageModel image );

	/**
     * <p>Deletes a previously uploaded image and its associated variants on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     */

	UploadStatus deleteImage( String imageId );

	/**
     * <p>Returns an absolute url as String for an image on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
	 *
	 * Note: This is the only way to get an url for an image identical to the one that was uploaded.
     */
    String getImageUrl( String imageId );


    /**
     * <p>Returns an absolute url as String for a variant image on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     * @param width the width of the image in pixels.
     * @return The absolute url of the computed image on the image server
     *
     * Note that even if the desired width is identical to the original width of the uploaded image,
     * there is no guarantee that a pixel-identical image equal to the image that was uploaded will be served from this url.
     */
	String getImageUrl( String imageId, int width);

    /**
     * <p>Returns an absolute url as String for a variant image on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     * @param width the width of the image in pixels.
     * @param height the height of the image in pixels.
     * @return The absolute url of the computed image on the image server.
     */

	String getImageUrl( String imageId, int width, int height);

    /**
     * <p>Returns an absolute url as String for a variant image on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     * @param width the width of the image in pixels.
     * @param height the height of the image in pixels.
     * @param version the version of the image, must be non-negative.
     * @return The absolute url of the computed image on the image server.
     */

	String getImageUrl( String imageId, int width, int height, int version );

    /**
     * <p>Returns an absolute url as String for a variant image on the image server.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     * @param width the width of the image in pixels.
     * @param height the height of the image in pixels.
     * @param version the version of the image, must be non-negative.
     * @param fileType the desired filetype.
     * @return The absolute url of the computed image on the image server.
     */

	String getImageUrl( String imageId, int width, int height, int version, String fileType );

    /**
     *  <p>Returns a String that represents the absolute url for the crop editing page of the specified image.</p>
     *
     * @param imageId the image id as returned by uploadImage or replaceImage.
     * @return The absolute url crop editing page for this image.
     */
    String getImageCropUrl(String imageId);

	/**
	 *  <p>Returns a String that represents the absolute url for the crop editing page of the specified image and version.</p>
	 *
	 * @param imageId the image id as returned by uploadImage or replaceImage.
	 * @param version the default version selected in the interface.
	 * @return The absolute url crop editing page for this image.
	 */

	String getImageCropUrl(String imageId, int version);
}