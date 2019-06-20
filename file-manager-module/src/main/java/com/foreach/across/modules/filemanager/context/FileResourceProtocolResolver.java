package com.foreach.across.modules.filemanager.context;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.services.FileManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * Allows resolving {@link com.foreach.across.modules.filemanager.services.FileManager} resources
 * as regular Spring resources, using the {@link ResourceLoader} interface.
 *
 * @author Arne Vandamme
 * @see com.foreach.across.modules.filemanager.extensions.FileResourceResolverRegistrar
 * @since 1.4.0
 */
@RequiredArgsConstructor
public class FileResourceProtocolResolver implements ProtocolResolver
{
	public static final String PROTOCOL = "axfs://";

	private final BeanFactory beanFactory;

	private FileManager fileManager;

	@Override
	public Resource resolve( String location, ResourceLoader resourceLoader ) {
		if ( location.startsWith( PROTOCOL ) ) {
			FileDescriptor descriptor = FileDescriptor.of( location );
			return requireFileManager().getFileResource( descriptor );
		}
		return null;
	}

	private FileManager requireFileManager() {
		if ( fileManager == null ) {
			fileManager = beanFactory.getBean( FileManager.class );
		}

		if ( fileManager == null ) {
			throw new IllegalStateException( "Unable to resolve FileManager resource - no FileManager component is available" );
		}

		return fileManager;
	}

	public Resource[] getResources( String locationPattern ) {
		return requireFileManager().findFiles( locationPattern ).toArray( new Resource[0] );
	}
}
