package com.foreach.imageserver.front;

import org.springframework.core.annotation.Order;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet 3.0 - registers a single DispatcherServlet.
 */
@Order(1)
public class DispatcherServletInitializer extends AbstractDispatcherServletInitializer {
    @Override
    protected WebApplicationContext createServletApplicationContext() {
        return new AnnotationConfigWebApplicationContext();
    }

    @Override
    protected String[] getServletMappings() {
        // All paths are mapped to this dispatcher servlet
        return new String[]{"/*"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(RootConfig.class);

        return context;
    }
}
