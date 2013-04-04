package com.foreach.imageserver.api.models;

import com.foreach.imageserver.api.business.UploadStatus;

/**
 * <p>A class that contains the result of when an image is uploaded to the imageserver.</p>
 *
 */
public class ImageServerUploadResult
{
	private final UploadStatus status;
	private final String imageId;

    /**
    * Constructs an <code>ImageServerUploadResult</code> given an {@link UploadStatus} and image id.
     *
     * @param status the {@link UploadStatus} object that contains the information of the upload status
     * @param imageId a unique image id that is used to identify an image on the imageserver
    */
	public ImageServerUploadResult( UploadStatus status, String imageId )
	{
		this.status = status;
		this.imageId = imageId;

	}

    /**
     *
     * @return returns true if the image has been uploaded successfully, false otherwise
     */
	public final boolean isUploaded()
	{
		return !status.isFailure();
	}

    /**
     *
     * @return returns an {@link UploadStatus} object that has the necessary status information
     */
	public final UploadStatus getStatus()
	{
		return status;
	}

    /**
     *
     * @return returns the image id if the image has been uploaded successful, otherwise an empty string ("") or null
     */
	public final String getImageId()
	{
		return imageId;
	}
}
