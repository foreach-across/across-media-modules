/*
 * Copyright 2014 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.filemanager.services;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestLocalFileRepositoryFactory
{
	private static final String TEMP_DIR = System.getProperty( "java.io.tmpdir" );
	private static final String ROOT_DIR = Paths.get( TEMP_DIR, UUID.randomUUID().toString() ).toString();

	private LocalFileRepositoryFactory factory;

	@Before
	public void create() {
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
		              ( (LocalFileRepository) one ).getRootFolderPath() );
		assertEquals( Paths.get( ROOT_DIR, "temp" ).toFile().getAbsolutePath(),
		              ( (LocalFileRepository) two ).getRootFolderPath() );
	}
}
