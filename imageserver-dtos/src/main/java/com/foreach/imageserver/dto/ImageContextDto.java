package com.foreach.imageserver.dto;

/**
 * @author Arne Vandamme
 */
public class ImageContextDto extends IdBasedEntityDto
{
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode( String code ) {
		this.code = code;
	}
}
