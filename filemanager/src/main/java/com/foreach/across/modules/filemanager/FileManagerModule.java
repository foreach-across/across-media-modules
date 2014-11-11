package com.foreach.across.modules.filemanager;

import com.foreach.across.core.AcrossModule;

public class FileManagerModule extends AcrossModule
{
	public static final String NAME = "FileManagerModule";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getDescription() {
		return "Provides a centralized service for file repository storage and management.";
	}
}
