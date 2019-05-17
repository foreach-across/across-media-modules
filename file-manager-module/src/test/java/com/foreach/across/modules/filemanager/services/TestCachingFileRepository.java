package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.services.CachingFileRepository.CachingFileRepositoryBuilder;
import org.assertj.core.api.AbstractBooleanAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@ExtendWith(MockitoExtension.class)
class TestCachingFileRepository
{
	@Mock
	private FileManager fileManager;

	@Mock
	private FileRepository targetRepository;

	@Mock
	private FileRepository cacheRepository;

	@Mock
	private BiFunction<FileDescriptor, FileRepository, FileResource> cacheResolver;

	private CachingFileRepository repository;

	@BeforeEach
	void setUp() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .build();
		repository.setFileManager( fileManager );
	}

	@Test
	void defaultCacheRepositoryIdIsTemp() {
		CachingFileRepository repositoryWithDefaultCacheRepositoryId
				= CachingFileRepository.builder().targetFileRepository( targetRepository ).cacheFileResourceResolver( cacheResolver ).build();
		assertThat( repositoryWithDefaultCacheRepositoryId.getCacheRepositoryId() ).isEqualTo( FileManager.TEMP_REPOSITORY );
	}

	@Test
	void repositoryIdIsFromTargetRepository() {
		when( targetRepository.getRepositoryId() ).thenReturn( "my-target" );
		assertThat( repository.getRepositoryId() ).isEqualTo( "my-target" );
	}

	@Test
	void targetRepository() {
		assertThat( repository.getTargetFileRepository() ).isSameAs( targetRepository );
	}

	@Test
	void pathGeneratorExceptionIfTargetNotAbstractFileRepository() {
		assertThat( repository.getPathGenerator() ).isNull();
		assertThatExceptionOfType( UnsupportedOperationException.class )
				.isThrownBy( () -> repository.setPathGenerator( mock( PathGenerator.class ) ) );
	}

	@Test
	void pathGeneratorForwardsToTargetIfAbstractFileRepository() {
		AbstractFileRepository target = mock( AbstractFileRepository.class );
		repository = repository.toBuilder().targetFileRepository( target ).build();

		PathGenerator pg = mock( PathGenerator.class );
		repository.setPathGenerator( pg );
		verify( target ).setPathGenerator( pg );
		when( target.getPathGenerator() ).thenReturn( pg );
		assertThat( repository.getPathGenerator() ).isSameAs( pg );
	}

	@Test
	void generatingFileDescriptorForwardsToTargetRepository() {
		FileDescriptor expected = FileDescriptor.of( "1:2:3" );
		when( targetRepository.generateFileDescriptor() ).thenReturn( expected );
		assertThat( repository.generateFileDescriptor() ).isEqualTo( expected );
	}

	@Test
	void cacheFileResourceIsResolvedAndSameIsReturnedOnMultipleCalls() {
		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );
		assertThat( fr.getCache() ).isSameAs( cacheFileResource );
		assertThat( fr.getTarget() ).isSameAs( targetFileResource );

		assertThat( repository.getFileResource( fd ) ).isSameAs( fr );
	}

	@Test
	void withGeneratedDescriptor() {
		repository = CachingFileRepository.withGeneratedFileDescriptor().targetFileRepository( targetRepository ).build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( FileManager.TEMP_REPOSITORY ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheRepository.createFileResource() ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );
		assertThat( fr.getCache() ).isSameAs( cacheFileResource );
		assertThat( fr.getTarget() ).isSameAs( targetFileResource );

	}

	@Test
	void withTranslatedDescriptor() {
		repository = CachingFileRepository.withTranslatedFileDescriptor().targetFileRepository( targetRepository ).build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( FileManager.TEMP_REPOSITORY ) ).thenReturn( cacheRepository );
		when( cacheRepository.getRepositoryId() ).thenReturn( "cache" );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheRepository.getFileResource( FileDescriptor.of( "cache:2:3" ) ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );
		assertThat( fr.getCache() ).isSameAs( cacheFileResource );
		assertThat( fr.getTarget() ).isSameAs( targetFileResource );
	}

	@Test
	void keepCacheOnEvict() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .removeCacheOnEvict( false )
		                                  .maxCacheItems( 0 )   // evict immediately
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );

		verifyZeroInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	void removeCacheOnEvict() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .maxCacheItems( 0 )   // evict immediately
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		when( cacheFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );

		verify( cacheFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	void removeCacheOnShutdown() {
		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		when( cacheFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );

		repository.shutdown();

		verify( cacheFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	void keepCacheOnShutdown() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .removeCacheOnShutdown( false )
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );

		repository.shutdown();

		verifyZeroInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	@SuppressWarnings("unchecked")
	void removedOnCleanupCache() {
		Function<CachedFileResource, Boolean> removalStrategy = mock( Function.class );
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .removalStrategy( removalStrategy )
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		when( cacheFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = (CachedFileResource) repository.getFileResource( fd );

		when( removalStrategy.apply( fr ) ).thenReturn( false );
		repository.cleanupCache();

		verifyZeroInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isSameAs( fr );

		when( removalStrategy.apply( fr ) ).thenReturn( true );
		repository.cleanupCache();

		verify( cacheFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	void timeBasedRemovalStrategy() {
		assertCacheRemoved( 0, 0, -1000, -2000 ).isFalse();
		assertCacheRemoved( 1000, 0, -2000, -2000 ).isTrue();
		assertCacheRemoved( 1000, 0, -100, -2000 ).isFalse();

		assertCacheRemoved( 0, 1000, -2000, -2000 ).isTrue();
		assertCacheRemoved( 0, 1000, -2000, -100 ).isFalse();
		assertCacheRemoved( 0, 1000, -2000, 0 ).isFalse();

		assertCacheRemoved( 1000, 1000, -2000, -2000 ).isTrue();
	}

	@Test
	void cleanupCachesInRegistry() {
		FileManagerImpl fileManager = new FileManagerImpl();
		CachingFileRepository cachingOne = mock( CachingFileRepository.class );
		when( cachingOne.getRepositoryId() ).thenReturn( "one" );
		CachingFileRepository cachingTwo = mock( CachingFileRepository.class );
		when( cachingTwo.getRepositoryId() ).thenReturn( "two " );
		FileRepository nonCaching = mock( FileRepository.class );
		when( nonCaching.getRepositoryId() ).thenReturn( "three " );

		fileManager.registerRepository( cachingOne );
		fileManager.registerRepository( nonCaching );
		fileManager.registerRepository( cachingTwo );

		CachingFileRepository.cleanupCaches( fileManager );

		verify( cachingOne ).cleanupCache();
		verify( cachingTwo ).cleanupCache();
	}

	private AbstractBooleanAssert<?> assertCacheRemoved( long maxUnusedDuration, long maxAge, long lastAccessTime, long cacheCreationTime ) {
		Function<CachedFileResource, Boolean> removalStrategy = CachingFileRepositoryBuilder.timeBasedRemovalStrategy( maxUnusedDuration, maxAge );

		CachedFileResource resource = mock( CachedFileResource.class, withSettings().lenient() );
		when( resource.getLastAccessTime() ).thenReturn( System.currentTimeMillis() + lastAccessTime );
		when( resource.getCacheCreationTime() ).thenReturn( System.currentTimeMillis() + cacheCreationTime );

		return assertThat( removalStrategy.apply( resource ) );
	}
}
