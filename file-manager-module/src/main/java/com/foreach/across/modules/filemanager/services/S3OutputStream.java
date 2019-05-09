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

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileStorageException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Because S3 does not support output streams, this class extends {@code ByteArrayOutputStream} and writes to
 * a {@code AmazonS3FileRepository} when flush is invoked.
 */
public class S3OutputStream extends ByteArrayOutputStream
{
	private AmazonS3FileRepository amazonS3FileRepository;
	private FileDescriptor fileDescriptor;

	public S3OutputStream( AmazonS3FileRepository amazonS3FileRepository, FileDescriptor fileDescriptor ) {
		this.amazonS3FileRepository = amazonS3FileRepository;
		this.fileDescriptor = fileDescriptor;
	}

	/**
	 * @throws IOException
	 * @throws FileStorageException
	 */
	@Override
	public void flush() throws IOException, FileStorageException {
		amazonS3FileRepository.save( fileDescriptor, new ByteArrayInputStream( toByteArray() ), true );
	}
}
