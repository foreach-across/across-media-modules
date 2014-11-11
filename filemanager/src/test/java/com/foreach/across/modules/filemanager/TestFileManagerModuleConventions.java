package com.foreach.across.modules.filemanager;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.AbstractAcrossModuleConventionsTest;

public class TestFileManagerModuleConventions extends AbstractAcrossModuleConventionsTest
{
	@Override
	protected boolean hasSettings() {
		return true;
	}

	@Override
	protected AcrossModule createModule() {
		return new FileManagerModule();
	}
}
