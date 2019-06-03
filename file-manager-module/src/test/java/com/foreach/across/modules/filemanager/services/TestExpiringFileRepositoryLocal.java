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

import com.foreach.across.modules.filemanager.business.FileResource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestExpiringFileRepositoryLocal extends BaseFileRepositoryTest
{
	private LocalFileRepository cacheRepository;

	@Override
	void createRepository() {
		cacheRepository = LocalFileRepository.builder().repositoryId( "cache" ).rootFolder( ROOT_DIR ).build();

		this.fileRepository = ExpiringFileRepository.builder().targetFileRepository( cacheRepository ).build();
	}

	@Test
	@SneakyThrows
	void shutdownExpiresItems() {
		FileResource resource = fileRepository.createFileResource();
		resource.copyFrom( RES_TEXTFILE );

		FileResource target = cacheRepository.getFileResource( resource.getDescriptor() );
		assertThat( target.exists() ).isTrue();
		assertThat( readResource( target ) ).isEqualTo( "some dummy text" );

		( (ExpiringFileRepository) fileRepository ).shutdown();

		assertThat( target.exists() ).isFalse();
	}
}