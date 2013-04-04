package com.foreach.imageserver.example.models;

import org.springframework.web.multipart.MultipartFile;

public class UploadModel
{
    private int applicationId;
    private int groupId;
	private String imageId;
    private MultipartFile imageData;

    public final int getApplicationId() {
        return applicationId;
    }

    public final void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public final int getGroupId() {
        return groupId;
    }

    public final void setGroupId(int groupId) {
        this.groupId = groupId;
    }

	public final String getImageId()
	{
		return imageId;
	}

	public final void setImageId( String imageId )
	{
		this.imageId = imageId;
	}

	public final MultipartFile getImageData() {
        return imageData;
    }

    public final void setImageData(MultipartFile imageData) {
        this.imageData = imageData;
    }
}
