package com.foreach.imageserver.dto;

import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertResultDto
{
	private int total;
	private Collection<Integer> pages;
	private Collection<String> keys;
	@Singular
	private Collection<ImageConvertResultTransformationDto> transforms;
}
