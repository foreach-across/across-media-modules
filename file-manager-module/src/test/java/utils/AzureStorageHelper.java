package utils;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
@Slf4j
@UtilityClass
public class AzureStorageHelper
{
	public final static AzuriteContainer azurite = new AzuriteContainer();

	static {
		azurite.start();
	}

	public static void createFolder( BlobServiceClient blobServiceClient, String containerName, String folderName ) {
		try {

			// create meta-data for your folder and set content-length to 0
			blobServiceClient
					.getBlobContainerClient( containerName )
					.getBlobClient( folderName )
					.upload( BinaryData.fromString( "" ) );
		}
		catch ( BlobStorageException  e ) {
			e.printStackTrace();
		}
	}
}
