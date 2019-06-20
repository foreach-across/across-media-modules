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

import com.amazonaws.services.s3.AmazonS3;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.nio.file.Paths;

/**
 * FileRepository which stores its files in an Amazon S3 bucket.
 * Optionally takes a {@link PathGenerator} for generating a folder structure when uploading a non-named file.
 *
 * @author Sander Van Loock, Arne Vandamme
 * @since 1.4.0
 */
@Slf4j
public class AmazonS3FileRepository extends AbstractFileRepository
{
	private final String bucketName;
	private final AmazonS3 amazonS3Client;
	private final TaskExecutor taskExecutor;

	@Builder
	AmazonS3FileRepository( @NonNull String repositoryId,
	                        @NonNull AmazonS3 amazonS3,
	                        @NonNull String bucketName,
	                        PathGenerator pathGenerator,
	                        TaskExecutor taskExecutor ) {
		super( repositoryId );
		setPathGenerator( pathGenerator );
		this.amazonS3Client = amazonS3;
		this.bucketName = bucketName;
		this.taskExecutor = taskExecutor != null ? taskExecutor : new SyncTaskExecutor();
	}

	@Override
	protected FileResource buildFileResource( FileDescriptor descriptor ) {
		return new AmazonS3FileResource( descriptor, amazonS3Client, bucketName, createObjectName( descriptor ), taskExecutor );
	}

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		String objectName = descriptor.getFolderId() != null ? descriptor.getFolderId() + "/" : "";
		return new AmazonS3FolderResource( descriptor, amazonS3Client, bucketName, objectName, taskExecutor );
	}

	private String createObjectName( FileDescriptor descriptor ) {
		String result;
		if ( descriptor.getFolderId() != null ) {
			result = Paths.get( descriptor.getFolderId(), descriptor.getFileId() ).toString();
		}
		else {
			result = Paths.get( descriptor.getFileId() ).toString();
		}

		return result.replace( "\\", "/" );
	}
}
