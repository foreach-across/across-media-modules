package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
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
		repository = CachingFileRepository.builder().targetFileRepository( target ).cacheFileResourceResolver( cacheResolver ).build();

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

		CachedFileResource fr = repository.getFileResource( fd );
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

		CachedFileResource fr = repository.getFileResource( fd );
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

		CachedFileResource fr = repository.getFileResource( fd );
		assertThat( fr.getCache() ).isSameAs( cacheFileResource );
		assertThat( fr.getTarget() ).isSameAs( targetFileResource );
	}

	@Test
	void keepCacheOnEvict() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .expireOnEvict( false )
		                                  .maxItemsToTrack( 0 )   // evict immediately
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = repository.getFileResource( fd );

		verifyNoInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	void expireOnEvict() {
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .maxItemsToTrack( 0 )   // evict immediately
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		when( cacheFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = repository.getFileResource( fd );

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

		CachedFileResource fr = repository.getFileResource( fd );

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
		                                  .expireOnShutdown( false )
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = repository.getFileResource( fd );

		repository.shutdown();

		verifyNoInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@Test
	@SuppressWarnings("unchecked")
	void removedOnCleanupCache() {
		Function<ExpiringFileResource, Boolean> expirationStrategy = mock( Function.class );
		repository = CachingFileRepository.builder()
		                                  .targetFileRepository( targetRepository )
		                                  .cacheRepositoryId( "cache" )
		                                  .cacheFileResourceResolver( cacheResolver )
		                                  .expirationStrategy( expirationStrategy )
		                                  .build();
		repository.setFileManager( fileManager );

		when( fileManager.getRepository( "cache" ) ).thenReturn( cacheRepository );

		FileResource targetFileResource = mock( FileResource.class );
		FileResource cacheFileResource = mock( FileResource.class );
		when( cacheFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );
		when( cacheResolver.apply( fd, cacheRepository ) ).thenReturn( cacheFileResource );

		CachedFileResource fr = repository.getFileResource( fd );

		when( expirationStrategy.apply( fr ) ).thenReturn( false );
		repository.expireTrackedItems();

		verifyNoInteractions( cacheFileResource );
		assertThat( repository.getFileResource( fd ) ).isSameAs( fr );

		when( expirationStrategy.apply( fr ) ).thenReturn( true );
		repository.expireTrackedItems();

		verify( cacheFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}
}
