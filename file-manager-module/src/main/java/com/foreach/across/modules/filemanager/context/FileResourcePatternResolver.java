package com.foreach.across.modules.filemanager.context;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@RequiredArgsConstructor
public class FileResourcePatternResolver implements ResourcePatternResolver
{
	private final FileResourceProtocolResolver protocolResolver;
	private final ResourcePatternResolver resourcePatternResolverDelegate;

	@Override
	public Resource[] getResources( String locationPattern ) throws IOException {
		if ( locationPattern.startsWith( FileResourceProtocolResolver.PROTOCOL ) ) {
			return protocolResolver.getResources( locationPattern );
		}
		return resourcePatternResolverDelegate.getResources( locationPattern );
	}

	@Override
	public Resource getResource( String location ) {
		return this.resourcePatternResolverDelegate.getResource( location );
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.resourcePatternResolverDelegate.getClassLoader();
	}
}
