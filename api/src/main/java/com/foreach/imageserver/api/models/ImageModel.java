package com.foreach.imageserver.api.models;

import org.springframework.web.multipart.MultipartFile;

/**
 * The <code>ImageModel</code> holds all the needed information that makes up an image. This includes a unique <code>imageid</code>
 * , a {@link MultipartFile} object that holds the actual image and finally the <code>extension</code> for example "jpg" or "gif".
 */
public class ImageModel
{
    private MultipartFile imageData;

    /**
     *
     * @return a {@link MultipartFile} object that holds the physical data and information of the uploaded image
     */
	public final MultipartFile getImageData() {
        return imageData;
    }

    /**
     *
     * @param imageData a {@link MultipartFile} object
     */
    public final void setImageData(MultipartFile imageData) {
        this.imageData = imageData;
    }
}
