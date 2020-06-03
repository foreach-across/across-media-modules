package com.foreach.across.modules.filemanager.extensions;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.modules.filemanager.context.FileResourcePatternResolver;
import com.foreach.across.modules.filemanager.context.FileResourceProtocolResolver;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * Configures both protocol and pattern resolver for file resources, based on the
 * presence of a {@link com.foreach.across.modules.filemanager.services.FileManager} bean in the bean factory.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@ModuleConfiguration(optional = true)
public class FileResourceResolverRegistrar implements BeanFactoryPostProcessor, ResourceLoaderAware
{
	private static final Field resourcePatternResolverField = ReflectionUtils.findField( AbstractApplicationContext.class, "resourcePatternResolver" );

	static {
		if ( resourcePatternResolverField != null ) {
			ReflectionUtils.makeAccessible( resourcePatternResolverField );
		}
	}

	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader( ResourceLoader resourceLoader ) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) {
		FileResourceProtocolResolver protocolResolver = new FileResourceProtocolResolver( beanFactory );

		if ( resourceLoader instanceof DefaultResourceLoader ) {
			( (DefaultResourceLoader) resourceLoader ).addProtocolResolver( protocolResolver );
		}
		else if ( resourceLoader instanceof ConfigurableApplicationContext ) {
			( (ConfigurableApplicationContext) resourceLoader ).addProtocolResolver( protocolResolver );
		}

		registerResourcePatternResolver( protocolResolver, resourceLoader );
	}

	/**
	 * If the resource loader is an {@link org.springframework.context.support.AbstractApplicationContext}, replaces the
	 * original resource pattern resolver by the custom implementation.
	 *
	 * @param protocolResolver to use
	 * @param resourceLoader   to attempt to register the pattern resolver
	 */
	@SuppressWarnings("squid:S3011")
	public static void registerResourcePatternResolver( FileResourceProtocolResolver protocolResolver, ResourceLoader resourceLoader ) {
		if ( resourceLoader instanceof AbstractApplicationContext && resourcePatternResolverField != null ) {
			try {
				ResourcePatternResolver currentResolver = (ResourcePatternResolver) resourcePatternResolverField.get( resourceLoader );
				resourcePatternResolverField.set( resourceLoader, new FileResourcePatternResolver( protocolResolver, currentResolver ) );
			}
			catch ( IllegalAccessException ignore ) {
				// unable to set value
			}
		}
	}
}
