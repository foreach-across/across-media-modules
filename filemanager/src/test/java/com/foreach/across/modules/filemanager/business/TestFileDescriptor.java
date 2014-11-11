package com.foreach.across.modules.filemanager.business;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestFileDescriptor
{
	@Test
	public void generateUri() {
		assertEquals( "default:test-file.txt", uri( "default", "test-file.txt" ) );
		assertEquals( "amazon-s3:2014/15/06:123-456-798", uri( "amazon-s3", "2014/15/06", "123-456-798" ) );
	}

	@Test
	public void parseUri() {
		FileDescriptor descriptor = parse( "default:test-file.txt" );

		assertEquals( "default", descriptor.getRepositoryId() );
		assertNull( descriptor.getFolderId() );
		assertEquals( "test-file.txt", descriptor.getFileId() );

		descriptor = parse( "amazon-s3:2014/15/06:123-456-798" );

		assertEquals( "amazon-s3", descriptor.getRepositoryId() );
		assertEquals( "2014/15/06", descriptor.getFolderId() );
		assertEquals( "123-456-798", descriptor.getFileId() );
	}

	private FileDescriptor parse( String uri ) {
		return new FileDescriptor( uri );
	}

	private String uri( String repository, String fileId ) {
		return new FileDescriptor( repository, fileId ).getUri();
	}

	private String uri( String repository, String folderId, String fileId ) {
		return new FileDescriptor( repository, folderId, fileId ).getUri();
	}
}
