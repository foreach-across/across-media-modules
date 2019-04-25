package com.foreach.imageserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertResultTransformationDto
{
	private String key;
	private byte[] image;
	private ImageTypeDto format;
}
