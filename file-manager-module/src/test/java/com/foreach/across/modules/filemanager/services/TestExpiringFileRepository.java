package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@SuppressWarnings("squid:S2925"/* remove use of thread sleep */)
@ExtendWith(MockitoExtension.class)
class TestExpiringFileRepository
{
	@Mock
	private FileRepository targetRepository;

	private ExpiringFileRepository repository;

	@BeforeEach
	void setUp() {
		repository = ExpiringFileRepository.builder()
		                                   .targetFileRepository( targetRepository )
		                                   .build();
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
		repository = ExpiringFileRepository.builder().targetFileRepository( target ).build();

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
	void expiringFileResourceIsResolvedAndSameIsReturnedOnMultipleCalls() {
		FileResource targetFileResource = mock( FileResource.class );
		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );
		assertThat( fr ).isNotNull();
		assertThat( repository.getFileResource( fd ) ).isSameAs( fr );
	}

	@Test
	@DisplayName("dont expire on evict but delete on finalization")
	void dontExpireOnEvict() {
		repository = ExpiringFileRepository.builder()
		                                   .targetFileRepository( targetRepository )
		                                   .maxItemsToTrack( 0 )   // evict immediately
		                                   .build();

		FileResource targetFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );

		verifyNoInteractions( targetFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );

		// cleanup on finalize
		when( targetFileResource.exists() ).thenReturn( true );
		fr = null;
		assertThat( fr ).isNull();

		runFinalization();

		// was fetched twice, same target mock will be finalized twice
		verify( targetFileResource, times( 2 ) ).delete();
	}

	@Test
	void expireOnEvict() {
		repository = ExpiringFileRepository.builder()
		                                   .targetFileRepository( targetRepository )
		                                   .expireOnEvict( true )
		                                   .maxItemsToTrack( 0 )   // evict immediately
		                                   .build();

		FileResource targetFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetFileResource.exists() ).thenReturn( true );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );

		verify( targetFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );

		// do not cleanup on finalize
		reset( targetFileResource );
		fr = null;
		assertThat( fr ).isNull();

		runFinalization();
		verifyNoInteractions( targetFileResource );
	}

	@Test
	void expireOnShutdown() {
		FileResource targetFileResource = mock( FileResource.class );
		when( targetFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );
		assertThat( fr ).isNotNull();

		repository.shutdown();

		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
		verify( targetFileResource ).delete();

		// shutdown again
		repository.shutdown();

		// do not cleanup on finalize
		reset( targetFileResource );
		fr = null;
		assertThat( fr ).isNull();

		runFinalization();
		verifyNoInteractions( targetFileResource );

	}

	@Test
	@DisplayName("dont expire on shutdown but delete on finalization")
	void dontExpireOnShutdownButCleanupOnGc() {
		repository = ExpiringFileRepository.builder()
		                                   .targetFileRepository( targetRepository )
		                                   .expireOnShutdown( false )
		                                   .build();

		FileResource targetFileResource = mock( FileResource.class );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );

		repository.shutdown();

		verifyNoInteractions( targetFileResource );
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );

		// cleanup on finalize
		when( targetFileResource.exists() ).thenReturn( true );
		fr = null;
		assertThat( fr ).isNull();

		runFinalization();
		verify( targetFileResource ).delete();
	}

	@Test
	@SuppressWarnings("unchecked")
	void expireTrackedItems() {
		Function<ExpiringFileResource, Boolean> expirationStrategy = mock( Function.class );
		repository = ExpiringFileRepository.builder()
		                                   .targetFileRepository( targetRepository )
		                                   .expirationStrategy( expirationStrategy )
		                                   .build();

		FileResource targetFileResource = mock( FileResource.class );
		when( targetFileResource.exists() ).thenReturn( true );

		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( targetRepository.getFileResource( fd ) ).thenReturn( targetFileResource );

		ExpiringFileResource fr = repository.getFileResource( fd );

		when( expirationStrategy.apply( fr ) ).thenReturn( false );
		repository.expireTrackedItems();

		verifyNoInteractions( targetFileResource );
		assertThat( repository.getFileResource( fd ) ).isSameAs( fr );

		when( expirationStrategy.apply( fr ) ).thenReturn( true );
		repository.expireTrackedItems();

		verify( targetFileResource ).delete();
		assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
	}

	@SneakyThrows
	private void runFinalization() {
		System.gc();
		System.runFinalization();
		Thread.sleep( 500 );
	}

	@Nested
	@DisplayName("Test ExpiringFileRepository resource implementation")
	class TrackedResource
	{
		@Mock
		private FileRepository targetRepository;

		@Mock
		private FileResource target;

		private ExpiringFileResource resource;
		private ExpiringFileRepository repository;

		@BeforeEach
		void setUp() {
			when( targetRepository.getFileResource( any() ) ).thenReturn( target );
			repository = ExpiringFileRepository.builder().targetFileRepository( targetRepository ).build();
			resource = repository.createFileResource();
		}

		@Test
		void fileDescriptor() {
			FileDescriptor fd = FileDescriptor.of( "1:2:3" );
			when( target.getDescriptor() ).thenReturn( fd );
			assertThat( resource.getDescriptor() ).isSameAs( fd );
			verifyNoMoreInteractions( target );
		}

		@Test
		@SneakyThrows
		void uri() {
			URI uri = URI.create( "test" );
			when( target.getURI() ).thenReturn( uri );
			assertThat( resource.getURI() ).isSameAs( uri );
			verifyNoMoreInteractions( target );
		}

		@Test
		@SneakyThrows
		void url() {
			URL url = URI.create( "http://localhost" ).toURL();
			when( target.getURL() ).thenReturn( url );
			assertThat( resource.getURL() ).isSameAs( url );
			verifyNoMoreInteractions( target );
		}

		@Test
		void description() {
			when( target.getDescription() ).thenReturn( "target description" );
			assertThat( resource.getDescription() )
					.isEqualTo( "axfs expiring resource (target description)" );
		}

		@Test
		void exists() {
			assertThat( resource.exists() ).isFalse();
			verify( target ).exists();

			when( target.exists() ).thenReturn( true );
			assertThat( resource.exists() ).isTrue();
			verify( target, times( 2 ) ).exists();
		}

		@Test
		void getFile() {
			assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.getFile() );
		}

		@Test
		void isReadable() {
			when( target.isReadable() ).thenReturn( true );
			assertThat( resource.isReadable() ).isTrue();
			verify( target ).isReadable();
			verifyNoMoreInteractions( target );
		}

		@Test
		void isWritable() {
			when( target.isWritable() ).thenReturn( true );
			assertThat( resource.isWritable() ).isTrue();
			verify( target ).isWritable();
			verifyNoMoreInteractions( target );
		}

		@Test
		void isOpen() {
			when( target.isOpen() ).thenReturn( true );
			assertThat( resource.isOpen() ).isTrue();
			verify( target ).isOpen();
			verifyNoMoreInteractions( target );
		}

		@Test
		void filename() {
			when( target.getFilename() ).thenReturn( "123" );
			assertThat( resource.getFilename() ).isEqualTo( "123" );
			verifyNoMoreInteractions( target );
		}

		@Test
		@SneakyThrows
		void lastModified() {
			when( target.lastModified() ).thenReturn( 123L );
			assertThat( resource.lastModified() ).isEqualTo( 123L );
			verifyNoMoreInteractions( target );
		}

		@Test
		@SneakyThrows
		void contentLength() {
			when( target.contentLength() ).thenReturn( 123L );
			assertThat( resource.contentLength() ).isEqualTo( 123L );
		}

		@Test
		void createRelative() {
			assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.createRelative( "relative" ) );
		}

		@Test
		void delete() {
			when( target.delete() ).thenReturn( true );
			assertThat( resource.delete() ).isTrue();
			verify( target ).delete();
		}

		@Test
		@SneakyThrows
		void inputStreamUpdatesAccessTime() {
			long ts = resource.getLastAccessTime();
			Thread.sleep( 500 );
			resource.getInputStream();
			assertThat( resource.getLastAccessTime() ).isGreaterThan( ts );
		}

		@Test
		@SneakyThrows
		void outputIsWrittenToTarget() {
			OutputStream targetOutputStream = mock( OutputStream.class );
			when( target.getOutputStream() ).thenReturn( targetOutputStream );

			try (OutputStream ignore = resource.getOutputStream()) {
				ignore.write( 123 );
			}

			verify( targetOutputStream ).write( 123 );
		}

		@Test
		@SneakyThrows
		void getCreationTime() {
			assertThat( resource.getCreationTime() ).isEqualTo( 0 );
			when( target.lastModified() ).thenReturn( 123L );
			assertThat( resource.getCreationTime() ).isEqualTo( 123L );
		}

		@Test
		@SneakyThrows
		void lastAccessTimeUpdated() {
			long accessTime = resource.getLastAccessTime();
			Thread.sleep( 100 );
			resource.exists();
			assertThat( resource.getLastAccessTime() ).isGreaterThan( accessTime );
			accessTime = resource.getLastAccessTime();

			Thread.sleep( 100 );
			resource.contentLength();
			assertThat( resource.getLastAccessTime() ).isGreaterThan( accessTime );
			accessTime = resource.getLastAccessTime();

			Thread.sleep( 100 );
			resource.lastModified();
			assertThat( resource.getLastAccessTime() ).isGreaterThan( accessTime );
		}

		@Test
		void deletedIsNoLongerTracked() {
			FileDescriptor fd = FileDescriptor.of( "1:2:3" );
			when( resource.getDescriptor() ).thenReturn( fd );
			ExpiringFileResource fr = repository.getFileResource( fd );
			reset( targetRepository );

			assertThat( repository.getFileResource( fd ) ).isSameAs( fr );
			verifyNoInteractions( targetRepository );

			fr.delete();
			when( targetRepository.getFileResource( any() ) ).thenReturn( mock( FileResource.class ) );
			assertThat( repository.getFileResource( fd ) ).isNotSameAs( fr );
			verify( targetRepository ).getFileResource( fd );
		}
	}
}
