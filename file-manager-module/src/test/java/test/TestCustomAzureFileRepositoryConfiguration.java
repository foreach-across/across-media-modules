package test;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.services.AzureFileRepository;
import com.foreach.across.modules.filemanager.services.CachingFileRepository;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.test.AcrossTestConfiguration;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;
import utils.AzureStorageHelper;
import utils.AzuriteContainer;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@DisplayName("Azure - Local file caching semantics")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
class TestCustomAzureFileRepositoryConfiguration
{
	private static final String CONTAINER_NAME = "caching-test";
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Test
	@SneakyThrows
	void cachedFileResource( @Autowired BlobServiceClient cloudBlobClient, @Autowired FileManager fileManager ) {
		FileResource myFile = fileManager.createFileResource( "az" );
		myFile.copyFrom( RES_TEXTFILE );

		FileDescriptor tempFileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, myFile.getDescriptor().getFolderId(),
		                                                       myFile.getDescriptor().getFileId() );
		FileResource tempFile = fileManager.getFileResource( tempFileDescriptor );
		assertThat( tempFile.exists() ).isTrue();

		assertThat( readResource( myFile ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "some dummy text" );
		assertThat(
				cloudBlobClient.getBlobContainerClient( CONTAINER_NAME )
				               .getBlobClient( "12/34/56/" + myFile.getDescriptor().getFileId() )
				               .downloadContent()
				               .toString()
		).isEqualTo( "some dummy text" );

		cloudBlobClient.getBlobContainerClient(CONTAINER_NAME)
		               .getBlobClient("12/34/56/" + myFile.getDescriptor().getFileId())
		               .upload(BinaryData.fromString("updated text"), true);

		tempFile.delete();
		assertThat( tempFile.exists() ).isFalse();
		assertThat( readResource( myFile ) ).isEqualTo( "updated text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "updated text" );
	}

	@SneakyThrows
	private String readResource( FileResource resource ) {
		try (InputStream is = resource.getInputStream()) {
			return StreamUtils.copyToString( is, Charset.defaultCharset() );
		}
	}

	@AcrossTestConfiguration
	static class RepositoriesConfiguration
	{

		@Bean
		@SneakyThrows
		BlobServiceClient cloudBlobClient( AzuriteContainer azurite ) {
			BlobServiceClient blobServiceClient = azurite.storageAccount();
			blobServiceClient.getBlobContainerClient( CONTAINER_NAME ).createIfNotExists();
			return blobServiceClient;
		}

		@Bean
		AzuriteContainer azurite() {
//			AzuriteContainer azurite = new AzuriteContainer();
//			azurite.addExposedPorts( 10000, 10001 );
//			azurite.setCommand( "azurite", "-l", "/data", "--blobHost", "0.0.0.0", "--queueHost", "0.0.0.0" );
//			azurite.waitingFor( Wait.forListeningPort() );
//			azurite.start();
//			return azurite;
			return AzureStorageHelper.azurite;
		}

		@Bean
		FileManagerModule fileManagerModule() {
			return new FileManagerModule();
		}

		@Bean
		FileRepository remoteRepository( BlobServiceClient blobServiceClient ) {
			return CachingFileRepository.withTranslatedFileDescriptor()
			                            .expireOnShutdown( true )
			                            .targetFileRepository(
					                            AzureFileRepository.builder()
					                                               .repositoryId( "az" )
					                                               .blobClient( blobServiceClient )
					                                               .containerName( CONTAINER_NAME )
					                                               .pathGenerator( () -> "12/34/56" )
					                                               .build()
			                            ).build();
		}
	}
}
