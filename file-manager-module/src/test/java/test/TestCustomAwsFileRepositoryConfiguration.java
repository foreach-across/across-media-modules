package test;

import com.amazonaws.services.s3.AmazonS3;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.business.FileDescriptor;
import com.foreach.across.modules.filemanager.business.FileResource;
import com.foreach.across.modules.filemanager.services.AmazonS3FileRepository;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;
import utils.AmazonS3Helper;

import java.io.InputStream;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Arne Vandamme
 * @since 1.4.0
 */
@DisplayName("AWS - Local file caching semantics")
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TestCustomAwsFileRepositoryConfiguration
{
	private static final String BUCKET_NAME = "caching-test";
	private static final Resource RES_TEXTFILE = new ClassPathResource( "textfile.txt" );

	@Test
	@SneakyThrows
	void cachedFileResource( @Autowired AmazonS3 amazonS3, @Autowired FileManager fileManager ) {
		FileResource myFile = fileManager.createFileResource( "s3" );
		myFile.copyFrom( RES_TEXTFILE );

		FileDescriptor tempFileDescriptor = FileDescriptor.of( FileManager.TEMP_REPOSITORY, myFile.getDescriptor().getFolderId(),
		                                                       myFile.getDescriptor().getFileId() );
		FileResource tempFile = fileManager.getFileResource( tempFileDescriptor );
		assertThat( tempFile.exists() ).isTrue();

		assertThat( readResource( myFile ) ).isEqualTo( "some dummy text" );
		assertThat( readResource( tempFile ) ).isEqualTo( "some dummy text" );

		assertThat( amazonS3.getObjectAsString( BUCKET_NAME, "12/34/56/" + myFile.getDescriptor().getFileId() ) ).isEqualTo( "some dummy text" );
		amazonS3.putObject( BUCKET_NAME, "12/34/56/" + myFile.getDescriptor().getFileId(), "updated text" );

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
		@Bean(destroyMethod = "shutdown")
		AmazonS3 amazonS3() {
			return AmazonS3Helper.createClientWithBuckets( BUCKET_NAME );
		}

		@Bean
		FileManagerModule fileManagerModule() {
			return new FileManagerModule();
		}

		@Bean
		FileRepository remoteRepository( AmazonS3 amazonS3 ) {
			return CachingFileRepository.withTranslatedFileDescriptor()
			                            .expireOnShutdown( true )
			                            .targetFileRepository(
					                            AmazonS3FileRepository.builder()
					                                                  .repositoryId( "s3" )
					                                                  .amazonS3( amazonS3 )
					                                                  .bucketName( BUCKET_NAME )
					                                                  .pathGenerator( () -> "12/34/56" )
					                                                  .build()
			                            ).build();
		}
	}
}
