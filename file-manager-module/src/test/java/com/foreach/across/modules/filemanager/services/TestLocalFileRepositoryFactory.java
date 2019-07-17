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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.junit.Assert.*;

class TestLocalFileRepositoryFactory
{
	@SuppressWarnings("WeakerAccess")
	@TempDir
	static File tempDir;

	private LocalFileRepositoryFactory factory = new LocalFileRepositoryFactory( tempDir.getAbsolutePath(), null );

	@Test
	void createRepositories() {
		FileRepository one = factory.create( "default" );
		FileRepository two = factory.create( "temp" );

		assertNotNull( one );
		assertNotNull( two );
		assertNotSame( one, two );
		assertTrue( one instanceof LocalFileRepository );
		assertTrue( two instanceof LocalFileRepository );

		assertTrue( new File( tempDir, "default" ).exists() );
		assertTrue( new File( tempDir, "temp" ).exists() );
		assertEquals( new File( tempDir, "default" ).getAbsolutePath(), ( (LocalFileRepository) one ).getRootFolderPath() );
		assertEquals( new File( tempDir, "temp" ).getAbsolutePath(), ( (LocalFileRepository) two ).getRootFolderPath() );
	}
}
