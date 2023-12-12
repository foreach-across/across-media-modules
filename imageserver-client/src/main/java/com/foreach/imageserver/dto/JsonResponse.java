package com.foreach.imageserver.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonResponse<T>
{
	public boolean success;
	public String errorMessage;
	public T result;
}
