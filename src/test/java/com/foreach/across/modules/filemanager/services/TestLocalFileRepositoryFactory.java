package com.foreach.across.modules.filemanager.services;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestLocalFileRepositoryFactory
{
	private static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
	private static final String ROOT_DIR = Paths.get( TEMP_DIR, UUID.randomUUID().toString() ).toString();

	private LocalFileRepositoryFactory factory;

	@Before
	public void create() throws IOException {
		cleanup();

		factory = new LocalFileRepositoryFactory( ROOT_DIR, null );
	}

	@After
	public void cleanup() {
		try {
			FileUtils.deleteDirectory( new File( ROOT_DIR ) );
		}
		catch ( Exception e ) {
			System.err.println( "Unit test could not cleanup files nicely" );
		}
	}

	@Test
	public void createRepositories() {
		FileRepository one = factory.create( "default" );
		FileRepository two = factory.create( "temp" );

		assertNotNull( one );
		assertNotNull( two );
		assertNotSame( one, two );
		assertTrue( one instanceof LocalFileRepository );
		assertTrue( two instanceof LocalFileRepository );

		assertTrue( Paths.get( ROOT_DIR, "default" ).toFile().exists() );
		assertTrue( Paths.get( ROOT_DIR, "temp" ).toFile().exists() );
		assertEquals( Paths.get( ROOT_DIR, "default" ).toFile().getAbsolutePath(),
		              ( (LocalFileRepository) one ).getRootFolder() );
		assertEquals( Paths.get( ROOT_DIR, "temp" ).toFile().getAbsolutePath(),
		              ( (LocalFileRepository) two ).getRootFolder() );
	}
}
