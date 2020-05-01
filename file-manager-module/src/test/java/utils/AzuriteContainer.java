package utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;

public class AzuriteContainer extends GenericContainer<AzuriteContainer>
{
	public AzuriteContainer() {
		super( "mcr.microsoft.com/azure-storage/azurite");
	}

	public CloudBlobClient getCloudBlobClient() {
		CloudStorageAccount developmentStorageAccount = CloudStorageAccount.getDevelopmentStorageAccount();
		UriComponents uri = UriComponentsBuilder.fromUri( developmentStorageAccount.getBlobEndpoint() ).port( getMappedPort( Service.BLOB.port ) ).host( getContainerIpAddress() ).build();
		return new CloudBlobClient( uri.toUri(), developmentStorageAccount.getCredentials() );
	}

	@RequiredArgsConstructor
	@Getter
	@FieldDefaults(makeFinal = true)
	public enum Service {
		BLOB(10000),
		QUEUE(10001);

		int port;
	}
}
