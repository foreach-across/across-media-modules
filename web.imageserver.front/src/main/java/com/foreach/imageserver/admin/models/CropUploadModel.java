package com.foreach.imageserver.admin.models;

public class CropUploadModel {

    private long imageId;
    private long cropId;
    private int formatId;

    private int left;
    private int top;
    private int width;
    private int height;

	private boolean fixedVersion;

    public final long getImageId() {
        return imageId;
    }

    public final void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public final long getCropId() {
        return cropId;
    }

    public final void setCropId(long cropId) {
        this.cropId = cropId;
    }

    public final int getFormatId() {
        return formatId;
    }

    public final void setFormatId(int formatId) {
        this.formatId = formatId;
    }

    public final int getLeft() {
        return left;
    }

    public final void setLeft(int left) {
        this.left = left;
    }

    public final int getTop() {
        return top;
    }

    public final void setTop(int top) {
        this.top = top;
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

	public final boolean isFixedVersion()
	{
		return fixedVersion;
	}

	public final void setFixedVersion( boolean fixedVersion )
	{
		this.fixedVersion = fixedVersion;
	}
}
