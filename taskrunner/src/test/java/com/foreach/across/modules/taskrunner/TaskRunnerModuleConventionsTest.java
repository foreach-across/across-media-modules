package com.foreach.across.modules.taskrunner;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.test.AbstractAcrossModuleConventionsTest;

/**
 * @author Arne Vandamme
 */
public class TaskRunnerModuleConventionsTest extends AbstractAcrossModuleConventionsTest
{
	@Override
	protected boolean hasSettings() {
		return true;
	}

	@Override
	protected AcrossModule createModule() {
		return new TaskRunnerModule();
	}
}
