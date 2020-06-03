package utils;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;

import java.io.IOException;
import java.net.URISyntaxException;

public class AzureStorageHelper
{
	public static void createFolder( CloudBlobClient cloudBlobClient, String containerName, String folderName ) {
		// create meta-data for your folder and set content-length to 0
		try {
			cloudBlobClient
					.getContainerReference( containerName )
					.getBlockBlobReference( folderName )
					.uploadText( "" );
		}
		catch ( StorageException | IOException | URISyntaxException e ) {
			e.printStackTrace();
		}
	}
}