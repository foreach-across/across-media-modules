package com.foreach.across.modules.filemanager.config;

import com.foreach.across.core.annotations.PostRefresh;
import com.foreach.across.modules.filemanager.FileManagerModuleSettings;
import com.foreach.across.modules.filemanager.services.AbstractExpiringFileRepository;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Task which executes periodically and calls {@link AbstractExpiringFileRepository#expireTrackedItems(FileRepositoryRegistry)}.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@Component
@ConditionalOnProperty(value = "fileManagerModule.expiration.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class FileRepositoryExpirationTask
{
	private final FileManagerModuleSettings settings;
	private final FileRepositoryRegistry fileRepositoryRegistry;

	private final ScheduledExecutorService monitorThread = Executors.newSingleThreadScheduledExecutor();

	private boolean started;

	@PostRefresh
	void start() {
		if ( !started && settings.getExpiration().isEnabled() ) {
			started = true;
			LOG.info( "Starting FileRepositoryExpirationTask with {} seconds delay", settings.getExpiration().getIntervalSeconds() );

			monitorThread.scheduleWithFixedDelay( this::cleanupCaches, 0, settings.getExpiration().getIntervalSeconds(), TimeUnit.SECONDS );
		}

	}

	private void cleanupCaches() {
		try {
			LOG.trace( "Scheduled execution of file repository expiration" );
			AbstractExpiringFileRepository.expireTrackedItems( fileRepositoryRegistry );
		}
		catch ( Exception e ) {
			LOG.error( "Exception during file repository expiration", e );
		}
	}

	@PreDestroy
	@SneakyThrows
	void stop() {
		monitorThread.shutdown();

		try {
			monitorThread.awaitTermination( 5, TimeUnit.SECONDS );
		}
		catch ( InterruptedException ie ) {
			LOG.warn( "Failed to wait for clean shutdown of lock monitor for expiration task" );
			throw ie;
		}
	}
}
