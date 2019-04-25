package com.foreach.imageserver.dto;

import lombok.*;

import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageConvertTargetDto
{
	private String key;
	@Singular
	private Collection<ImageTransformDto> transforms;
}
