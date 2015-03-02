package com.foreach.across.webapp.spring.batch;

import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.across.modules.web.servlet.AbstractAcrossServletInitializer;
import com.foreach.across.webapp.spring.batch.config.TestWebConfiguration;

/**
 * @author Arne Vandamme
 */
public class WebAppInitializer extends AbstractAcrossServletInitializer
{
	@Override
	protected void configure( AcrossWebApplicationContext applicationContext ) {
		applicationContext.register( TestWebConfiguration.class );
	}
}