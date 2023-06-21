package com.foreach.across.modules.filemanager.services;

import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.business.FolderDescriptor;
import com.foreach.across.modules.filemanager.business.FolderResource;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import lombok.Builder;
import lombok.NonNull;

import java.nio.file.Paths;

public class AzureFileRepository extends AbstractFileRepository
{
	private final String containerName;
	private final CloudBlobClient blobClient;

	@Builder
	AzureFileRepository( @NonNull String repositoryId,
	                     @NonNull CloudBlobClient blobClient,
	                     @NonNull String containerName,
	                     PathGenerator pathGenerator ) {
		super( repositoryId );
		setPathGenerator( pathGenerator );
		this.blobClient = blobClient;
		this.containerName = containerName;
	}

	public String getContainerName() {
		return containerName;
	}

	public CloudBlobClient getBlobClient() {
		return blobClient;
	}

	@Override
	protected AzureFileResource buildFileResource( FileDescriptor descriptor ) {
		return new AzureFileResource( descriptor, blobClient, containerName, createObjectName( descriptor ) );
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

	@Override
	protected FolderResource buildFolderResource( FolderDescriptor descriptor ) {
		String objectName = descriptor.getFolderId() != null ? descriptor.getFolderId() + "/" : "";
		return new AzureFolderResource( descriptor, blobClient, containerName, objectName );
	}
}
