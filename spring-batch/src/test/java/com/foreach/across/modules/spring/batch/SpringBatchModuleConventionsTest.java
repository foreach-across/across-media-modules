package com.foreach.across.modules.spring.batch;

import com.foreach.across.core.AcrossModule;
import com.foreach.across.modules.spring.batch.SpringBatchModule;
import com.foreach.across.test.AbstractAcrossModuleConventionsTest;

/**
 * @author Arne Vandamme
 */
public class SpringBatchModuleConventionsTest extends AbstractAcrossModuleConventionsTest
{
	@Override
	protected boolean hasSettings() {
		return true;
	}

	@Override
	protected AcrossModule createModule() {
		return new SpringBatchModule();
	}
}
