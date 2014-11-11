package com.foreach.across.modules.filemanager.business;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileData
{
	OutputStream getOutputStream();

	void copyFrom( File file );

	void copyFrom( InputStream inputStream );

	File getAsFile();

	boolean exists();

	boolean delete();
}
