package com.foreach.imageserver.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertDto
{
	private byte[] image;
	private String pages;
	@Singular
	private Map<String, List<ImageTransformDto>> transformations;
}
