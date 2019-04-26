package com.foreach.imageserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertResultDto
{
	private int total;
	private Collection<Integer> pages;
	private Collection<String> keys;
	private Map<String, ImageConvertResultTransformationDto> transforms;
}
