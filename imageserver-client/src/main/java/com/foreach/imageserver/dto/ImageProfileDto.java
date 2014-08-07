package com.foreach.imageserver.dto;

public class ImageProfileDto extends IdBasedEntityDto
{
	private String name;

	public String getName() {
		return name;
	}

	public void setName( String name ) {
		this.name = name;
	}
}
