package com.foreach.imageserver.admin.rendering;

import java.io.File;

public class ImageRenderingResult
{
	private final File targetFile;
	private final Long cropId;

	public ImageRenderingResult( File targetFile, Long cropId )
	{
		this.targetFile = targetFile;
		this.cropId = cropId;
	}

	public final File getTargetFile()
	{
		return targetFile;
	}

	public final Long getCropId()
	{
		return cropId;
	}

	// Normally, this will only be called from within an exception handler, so avoid throwing exceptions.
	public final void removeTargetFile()
	{
		if( targetFile != null ) {
			try {
				targetFile.delete();
			} catch ( Exception  exception ) {

			}
		}
	}
}
