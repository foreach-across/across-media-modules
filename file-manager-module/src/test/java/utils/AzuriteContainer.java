package utils;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.ctc.wstx.shaded.msv_core.util.Uri;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.util.UriComponentsBuilder;
import org.testcontainers.containers.GenericContainer;

import java.net.URI;
import java.util.function.Supplier;

public class AzuriteContainer extends GenericContainer<AzuriteContainer>
{
	private CloudStorageAccount developmentStorageAccount = CloudStorageAccount.getDevelopmentStorageAccount();

	public AzuriteContainer() {
		super( "mcr.microsoft.com/azure-storage/azurite" );
		for ( Service service : Service.values() ) {
			addExposedPort( service.getPort() );
		}
	}

	public BlobServiceClient storageAccount() {
		String containerIpAddress = getContainerIpAddress();
//		String connectionString =
//				"DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey= ;BlobEndpoint=http://localhost:10000/devstoreaccount1;";
		return new BlobServiceClientBuilder()
				.connectionString( connectionString )
				.buildClient();
	}

	private URI fromUri( Supplier<URI> endpoint, Supplier<Service> service ) {
		return UriComponentsBuilder.fromUri( endpoint.get() )
		                           .port( getMappedPort( service.get().port ) )
		                           .host( getContainerIpAddress() )
		                           .build()
		                           .toUri();
	}

	@RequiredArgsConstructor
	@Getter
	@FieldDefaults(makeFinal = true)
	public enum Service
	{
		BLOB( 10000 ),
		QUEUE( 10001 ),
		TABLE( 10002 );

		int port;
	}
}
