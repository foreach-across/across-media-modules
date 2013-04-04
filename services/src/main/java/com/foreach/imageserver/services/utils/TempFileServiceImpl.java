package com.foreach.imageserver.services.utils;

import java.io.File;
import java.io.IOException;

public class TempFileServiceImpl implements TempFileService
{
	private File tempDir;

	public final void setTempDir( String tmpDirPath ) throws IOException
	{
		tempDir = new File( tmpDirPath );
		if( !tempDir.exists() ) {
			tempDir.mkdirs();
		}
	}

	public final File tmpFile( String prefix, String suffix ) throws IOException
	{
		return File.createTempFile( prefix, suffix, tempDir );
	}

	public final boolean deleteFile( String path )
	{
		File targetFile = new File( path );
		return targetFile.delete();
	}
}
