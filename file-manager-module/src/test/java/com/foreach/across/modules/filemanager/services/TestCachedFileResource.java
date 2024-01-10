package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@SuppressWarnings("squid:S2925"/* remove use of thread sleep */)
@ExtendWith(MockitoExtension.class)
class TestCachedFileResource
{
	@Mock
	private FileResource target;

	@Mock
	private FileResource cache;

	@Mock
	private CachingFileRepository cachingFileRepository;

	private CachedFileResource resource;

	@BeforeEach
	void setUp() {
		resource = new CachedFileResource( target, cache, cachingFileRepository );
	}

	@Test
	void fileDescriptor() {
		FileDescriptor fd = FileDescriptor.of( "1:2:3" );
		when( target.getDescriptor() ).thenReturn( fd );
		assertThat( resource.getDescriptor() ).isSameAs( fd );
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	void folderResource() {
		FolderResource original = mock( FolderResource.class );
		when( target.getFolderResource() ).thenReturn( original );

		FolderResource wrapped = mock( FolderResource.class );
		when( cachingFileRepository.createExpiringFolderResource( original ) ).thenReturn( wrapped );

		assertThat( resource.getFolderResource() ).isSameAs( wrapped );
	}

	@Test
	@SneakyThrows
	void uri() {
		URI uri = URI.create( "test" );
		when( target.getURI() ).thenReturn( uri );
		assertThat( resource.getURI() ).isSameAs( uri );
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	@SneakyThrows
	void url() {
		URL url = URI.create( "http://localhost" ).toURL();
		when( target.getURL() ).thenReturn( url );
		assertThat( resource.getURL() ).isSameAs( url );
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	void description() {
		when( target.getDescription() ).thenReturn( "target description" );
		when( cache.getDescription() ).thenReturn( "cache description" );
		assertThat( resource.getDescription() )
				.isEqualTo( "axfs cached resource (cache description : target description)" );
	}

	@Test
	void exists() {
		assertThat( resource.exists() ).isFalse();
		verify( target ).exists();
		verify( cache ).exists();

		when( target.exists() ).thenReturn( true );
		assertThat( resource.exists() ).isTrue();
		verify( cache, times( 2 ) ).exists();
		verify( target, times( 2 ) ).exists();

		when( cache.exists() ).thenReturn( true );
		assertThat( resource.exists() ).isTrue();
		verify( cache, times( 3 ) ).exists();
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
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	void isWritable() {
		when( target.isWritable() ).thenReturn( true );
		assertThat( resource.isWritable() ).isTrue();
		verify( target ).isWritable();
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	void isOpen() {
		when( target.isOpen() ).thenReturn( true );
		assertThat( resource.isOpen() ).isTrue();
		verify( target ).isOpen();
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	void filename() {
		when( target.getFilename() ).thenReturn( "123" );
		assertThat( resource.getFilename() ).isEqualTo( "123" );
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	@SneakyThrows
	void lastModified() {
		when( target.lastModified() ).thenReturn( 123L );
		assertThat( resource.lastModified() ).isEqualTo( 123L );
		verifyNoMoreInteractions( target, cache );
	}

	@Test
	@SneakyThrows
	void contentLength() {
		when( target.contentLength() ).thenReturn( 123L );
		when( cache.contentLength() ).thenReturn( 456L );

		assertThat( resource.contentLength() ).isEqualTo( 123L );
		when( cache.exists() ).thenReturn( true );
		assertThat( resource.contentLength() ).isEqualTo( 456L );
		when( cache.exists() ).thenReturn( false );
		assertThat( resource.contentLength() ).isEqualTo( 123L );
	}

	@Test
	void createRelative() {
		assertThatExceptionOfType( UnsupportedOperationException.class ).isThrownBy( () -> resource.createRelative( "relative" ) );
	}

	@Test
	void delete() {
		when( cache.exists() ).thenReturn( true );
		when( target.delete() ).thenReturn( true );
		assertThat( resource.delete() ).isTrue();

		InOrder inOrder = inOrder( target, cache );
		inOrder.verify( cache ).exists();
		inOrder.verify( cache ).delete();
		inOrder.verify( target ).delete();
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
	void inputStreamUsesCacheIfExists() {
		InputStream is = mock( InputStream.class );
		when( cache.exists() ).thenReturn( true );
		when( cache.getInputStream() ).thenReturn( is );

		assertThat( resource.getInputStream() ).isSameAs( is );
		verifyNoInteractions( target );
	}

	@Test
	@SneakyThrows
	void inputStreamIsCopiedIfCacheNotExists() {
		InputStream is = mock( InputStream.class );
		when( cache.getInputStream() ).thenReturn( is );

		assertThat( resource.getInputStream() ).isSameAs( is );
		verify( target ).copyTo( cache );
		verifyNoMoreInteractions( target );
	}

	@Test
	@SneakyThrows
	void outputIsWrittenToCacheAndTarget() {
		OutputStream targetOutputStream = mock( OutputStream.class );
		OutputStream cacheOutputStream = mock( OutputStream.class );
		when( target.getOutputStream() ).thenReturn( targetOutputStream );
		when( cache.getOutputStream() ).thenReturn( cacheOutputStream );

		try (OutputStream ignore = resource.getOutputStream()) {
			ignore.write( 123 );
		}
		verify( cacheOutputStream ).write( 123 );
		verify( targetOutputStream ).write( 123 );
	}

	@Test
	void flushCache() {
		assertThat( resource.flushCache() ).isFalse();
		verify( cache ).exists();
		verifyNoMoreInteractions( cache );

		when( cache.delete() ).thenReturn( true );
		when( cache.exists() ).thenReturn( true );
		assertThat( resource.flushCache() ).isTrue();
		verify( cache ).delete();
	}

	@Test
	@SneakyThrows
	void getCacheCreationTime() {
		when( cache.lastModified() ).thenReturn( 123L );
		assertThat( resource.getCreationTime() ).isEqualTo( 0 );
		when( cache.exists() ).thenReturn( true );
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
	@SneakyThrows
	void cacheIsRemovedIfExceptionDuringWrite() {
		InputStream is = mock( InputStream.class );
		when( is.read( any( byte[].class ) ) ).thenThrow( new IOException() );

		when( cache.exists() ).thenReturn( true );

		assertThatExceptionOfType( IOException.class )
				.isThrownBy( () -> resource.copyFrom( is ) );

		verify( cache ).delete();
	}
}
