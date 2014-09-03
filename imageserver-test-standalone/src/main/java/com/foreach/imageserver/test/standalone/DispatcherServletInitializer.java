package com.foreach.imageserver.test.standalone;

import com.foreach.across.modules.web.context.AcrossWebApplicationContext;
import com.foreach.imageserver.test.standalone.config.StandaloneWebConfiguration;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet 3.0 - registers a single DispatcherServlet.
 */
public class DispatcherServletInitializer extends AbstractDispatcherServletInitializer
{
	@Override
	protected WebApplicationContext createServletApplicationContext() {
		return new AcrossWebApplicationContext();
	}

	@Override
	protected String[] getServletMappings() {
		// All paths are mapped to this dispatcher servlet
		return new String[] { "/*" };
	}

	@Override
	protected WebApplicationContext createRootApplicationContext() {
		AcrossWebApplicationContext context = new AcrossWebApplicationContext();
		context.register( StandaloneWebConfiguration.class );

		return context;
	}
}
