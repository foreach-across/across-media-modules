package com.foreach.imageserver.services.utils;

import java.io.File;
import java.io.IOException;

public interface TempFileService
{
	File tmpFile( String prefix, String suffix ) throws IOException;

	boolean deleteFile( String path );
}
