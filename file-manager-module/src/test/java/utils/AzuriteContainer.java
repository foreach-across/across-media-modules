package utils;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
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

	public AzuriteContainer() {
		super( "mcr.microsoft.com/azure-storage/azurite" );
		for ( Service service : Service.values() ) {
			addExposedPort( service.getPort() );
		}
	}

	public BlobServiceClient storageAccount() {
		return new BlobServiceClientBuilder()
				.endpoint( String.format( "http://127.0.0.1:%s/devstoreaccount1", getMappedPort( Service.BLOB.port ) ) )
				.credential( new StorageSharedKeyCredential( "devstoreaccount1",
				                                             "Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==" ) )
				.buildClient();
	}

	private URI fromUri( Supplier<URI> endpoint, Supplier<Service> service ) {
		return UriComponentsBuilder.fromUri( endpoint.get() )
		                           .port( getMappedPort( service.get().port ) )
		                           .host( getHost() )
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
