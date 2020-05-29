package utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageUri;
import com.microsoft.azure.storage.analytics.CloudAnalyticsClient;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class AzuriteContainer extends GenericContainer<AzuriteContainer>
{
	private CloudStorageAccount developmentStorageAccount = CloudStorageAccount.getDevelopmentStorageAccount();

	public AzuriteContainer() {
		super( "mcr.microsoft.com/azure-storage/azurite");
	}

	public CloudStorageAccount storageAccount() throws URISyntaxException {
		URI blob = fromUri( () -> developmentStorageAccount.getBlobEndpoint(), () -> Service.BLOB ); //10000
		URI queue = fromUri( () -> developmentStorageAccount.getQueueEndpoint(), () -> Service.QUEUE ); //10001
		//URI table = fromUri( () -> developmentStorageAccount.getTableEndpoint(), () -> Service.TABLE ); //10001
		return new CloudStorageAccount( developmentStorageAccount.getCredentials(), blob, queue, developmentStorageAccount.getTableEndpoint() );
	}

	private URI fromUri( Supplier<URI> endpoint, Supplier<Service> service ) {
		return UriComponentsBuilder.fromUri( endpoint.get() )
		                           .port( getMappedPort( service.get().port ) )
		                           .host( getContainerIpAddress() )
		                           .build()
		                           .toUri();
	}

	public CloudAnalyticsClient getCloudAnalyticsClient() {
		StorageUri x = developmentStorageAccount.getTableStorageUri();
		StorageUri y =  developmentStorageAccount.getBlobStorageUri();
		UriComponents uri = UriComponentsBuilder.fromUri( developmentStorageAccount.getBlobEndpoint() ).port( getMappedPort( Service.BLOB.port ) ).host( getContainerIpAddress() ).build();
		return new CloudAnalyticsClient( x, y, developmentStorageAccount.getCredentials() );
	}

	@RequiredArgsConstructor
	@Getter
	@FieldDefaults(makeFinal = true)
	public enum Service {
		BLOB(10000),
		QUEUE(10001),
		TABLE( 10002 );

		int port;
	}
}
