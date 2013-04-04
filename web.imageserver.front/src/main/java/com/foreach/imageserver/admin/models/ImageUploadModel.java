package com.foreach.imageserver.admin.models;


import org.springframework.web.multipart.MultipartFile;

public class ImageUploadModel
{
    private String userKey;
	private String imageKey;
	private MultipartFile image;
	private String originalFilename;

    public final String getUserKey() {
        return userKey;
    }

    public final void setUserKey(String userKey) {
        this.userKey = userKey;
    }

	public final String getImageKey()
	{
		return imageKey;
	}

	public final void setImageKey( String imageKey )
	{
		this.imageKey = imageKey;
	}

	public final  MultipartFile getImage()
	{
		return image;
	}

	public final void setImage(MultipartFile image)
	{
		this.image = image;
	}

	// This is explicitly added as metadata, because the originalFilename in image
	// may be not reliable, due to  e.g. the client operating in a store-and-forward mode
	// necessitating the use of tmp files

	public final String getOriginalFilename()
	{
		return originalFilename;
	}

	public final void setOriginalFilename( String originalFilename )
	{
		this.originalFilename = originalFilename;
	}
}