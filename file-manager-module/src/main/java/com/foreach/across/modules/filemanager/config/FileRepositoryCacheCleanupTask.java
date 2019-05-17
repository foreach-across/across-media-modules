package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.services.CachingFileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Task which executes periodically and calls {@link com.foreach.across.modules.filemanager.services.CachingFileRepository#cleanupCaches(FileRepositoryRegistry)}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@Component
@ConditionalOnProperty(value = "fileManagerModule.cacheCleanup.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings( "unused" )
public class FileRepositoryCacheCleanupTask
{
	private final FileManagerModuleSettings settings;
	private final FileRepositoryRegistry fileRepositoryRegistry;

	private final ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor();

	private boolean started;

	@PostRefresh
	void start() {
		if ( !started && settings.getCacheCleanup().isEnabled() ) {
			started = true;
			LOG.info( "Starting FileRepositoryCacheCleanupTask with {} seconds delay", settings.getCacheCleanup().getDelaySeconds() );

			monitorThread.scheduleWithFixedDelay( this::cleanupCaches, 0, settings.getCacheCleanup().getDelaySeconds(), TimeUnit.SECONDS );
		}

	}

	private void cleanupCaches() {
		try {
			LOG.trace( "Scheduled execution of file repository cache cleanup" );
			CachingFileRepository.cleanupCaches( fileRepositoryRegistry );
		}
		catch ( Exception e ) {
			LOG.error( "Exception during file repository cache cleanup", e );
		}
	}

	@PreDestroy
	void stop() {
		monitorThread.shutdown();

		try {
			monitorThread.awaitTermination( 5, TimeUnit.SECONDS );
		}
		catch ( InterruptedException ie ) {
			LOG.warn( "Failed to wait for clean shutdown of lock monitor for cache cleanup task" );
		}
	}
}
