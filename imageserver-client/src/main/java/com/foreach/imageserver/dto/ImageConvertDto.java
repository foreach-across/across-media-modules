package com.foreach.imageserver.dto;

import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertDto
{
	private byte[] image;
	private ImageTypeDto format;
	private String pages;
	@Singular
	private Collection<ImageConvertTargetDto> targets;
}
