package com.foreach.imageserver.test.embedded.application.extensions;

import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import com.foreach.across.modules.filemanager.services.LocalFileRepository;
import com.foreach.imageserver.core.config.ServicesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;

/**
 * @author Steven Gentens
 * @since 5.0.0
 */
@ModuleConfiguration(FileManagerModule.NAME)
@OrderInModule(Ordered.HIGHEST_PRECEDENCE)
public class LocalFileRepositoriesConfiguration
{
	@Autowired
	public void registerFileRepository( FileRepositoryRegistry fileRepositoryRegistry ) {
		LocalFileRepository fileRepository = new LocalFileRepository( ServicesConfiguration.IMAGESERVER_ORIGINALS_REPOSITORY, "local-data/image-originals" );
		fileRepositoryRegistry.registerRepository( fileRepository );
	}
}
