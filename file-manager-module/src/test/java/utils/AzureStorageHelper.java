package utils;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AzureStorageHelper
{
	public final static AzuriteContainer azurite = new AzuriteContainer();

	static {
		azurite.start();
	}

	public static void createFolder( BlobServiceClient blobServiceClient, String containerName, String folderName ) {
		// create meta-data for your folder and set content-length to 0
		blobServiceClient
				.getBlobContainerClient( containerName )
				.getBlobClient( folderName )
				.upload( BinaryData.fromString( "" ) );
	}
}
