package com.foreach.across.modules.filemanager.services;

import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static com.foreach.across.modules.filemanager.services.AbstractExpiringFileRepository.timeBasedExpirationStrategy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
class TestAbstractExpiringFileRepository
{
	@Test
	void timeBasedExpiration() {
		assertExpires( 0, 0, -1000, -2000 ).isFalse();
		assertExpires( 1000, 0, -2000, -2000 ).isTrue();
		assertExpires( 1000, 0, -100, -2000 ).isFalse();

		assertExpires( 0, 1000, -2000, -2000 ).isTrue();
		assertExpires( 0, 1000, -2000, -100 ).isFalse();
		assertExpires( 0, 1000, -2000, 0 ).isFalse();

		assertExpires( 1000, 1000, -2000, -2000 ).isTrue();
	}

	private AbstractBooleanAssert<?> assertExpires( long maxUnusedDuration, long maxAge, long lastAccessTime, long cacheCreationTime ) {
		Function<ExpiringFileResource, Boolean> expirationStrategy = timeBasedExpirationStrategy( maxUnusedDuration, maxAge );

		ExpiringFileResource resource = mock( ExpiringFileResource.class, withSettings().lenient() );
		when( resource.getLastAccessTime() ).thenReturn( System.currentTimeMillis() + lastAccessTime );
		when( resource.getCreationTime() ).thenReturn( System.currentTimeMillis() + cacheCreationTime );

		return assertThat( expirationStrategy.apply( resource ) );
	}

	@Test
	void expireTrackedItemsInRegistry() {
		FileManagerImpl fileManager = new FileManagerImpl();
		AbstractExpiringFileRepository cachingOne = mock( AbstractExpiringFileRepository.class );
		when( cachingOne.getRepositoryId() ).thenReturn( "one" );
		AbstractExpiringFileRepository cachingTwo = mock( AbstractExpiringFileRepository.class );
		when( cachingTwo.getRepositoryId() ).thenReturn( "two " );
		FileRepository nonCaching = mock( FileRepository.class );
		when( nonCaching.getRepositoryId() ).thenReturn( "three " );

		fileManager.registerRepository( cachingOne );
		fileManager.registerRepository( nonCaching );
		fileManager.registerRepository( cachingTwo );

		AbstractExpiringFileRepository.expireTrackedItems( fileManager );

		verify( cachingOne ).expireTrackedItems();
		verify( cachingTwo ).expireTrackedItems();
	}
}
