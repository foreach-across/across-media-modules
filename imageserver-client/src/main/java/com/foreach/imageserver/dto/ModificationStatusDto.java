package com.foreach.imageserver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModificationStatusDto
{
	private String imageId;
	private boolean modified;
}
