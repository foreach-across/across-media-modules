package com.foreach.imageserver.admin.models;

import com.foreach.imageserver.business.image.ServableImageData;
import org.springframework.web.multipart.MultipartFile;

public class ImageModel
{
    private ServableImageData image;
	private MultipartFile imageContent;
    private String imageLink;

    public final ServableImageData getImage() {
        return image;
    }

    public final void setImage(ServableImageData image) {
        this.image = image;
    }

    public final MultipartFile getImageContent() {
        return imageContent;
    }

    public final void setImageContent(MultipartFile imageContent) {
        this.imageContent = imageContent;
    }

    public final String getImageLink() {
        return imageLink;
    }

    public final void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
