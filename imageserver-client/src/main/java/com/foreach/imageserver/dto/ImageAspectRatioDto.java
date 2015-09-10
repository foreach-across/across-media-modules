package com.foreach.imageserver.dto;

/**
 * @author niels
 * @since 10/09/2015
 */
public class ImageAspectRatioDto
{
	private String ratio;

	public ImageAspectRatioDto() {
	}

	public ImageAspectRatioDto( String ratio ) {
		this.ratio = ratio;
	}

	public String getRatio() {
		return ratio;
	}

	public void setRatio( String ratio ) {
		this.ratio = ratio;
	}
}
