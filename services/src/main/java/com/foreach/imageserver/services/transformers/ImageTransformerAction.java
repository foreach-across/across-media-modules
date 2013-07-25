package com.foreach.imageserver.services.transformers;

public abstract class ImageTransformerAction<T>
{
	private T result;

	public T getResult() {
		return result;
	}

	public void setResult( T result ) {
		this.result = result;
	}
}
