package com.foreach.across.samples.filemanager;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.foreach.across.config.AcrossApplication;
import com.foreach.across.modules.adminweb.AdminWebModule;
import com.foreach.across.modules.entity.EntityModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.services.*;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.testcontainers.AzuriteContainer;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.testcontainers.containers.localstack.LocalStackContainer;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.S3;

/**
 * @author Steven Gentens
 */
@AcrossApplication(modules = { AdminWebModule.NAME, FileManagerModule.NAME, AcrossWebModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME,
                               EntityModule.NAME })
public class FileManagerTestApplication
{
	@Bean
	@Profile("azure")
	public AzuriteContainer azureStorage() {
		AzuriteContainer container = new AzuriteContainer();
		container.start();
		return container;
	}

	@Bean
	@Profile("azure")
	public FileRepository fileRepositoryAzure( AzuriteContainer azuriteContainer ) {
		CloudBlobClient cloudBlobClient = azuriteContainer.storageAccount().createCloudBlobClient();
		try {
			cloudBlobClient.getContainerReference( "car-files" ).createIfNotExists();
		}
		catch ( StorageException | URISyntaxException e ) {
			e.printStackTrace();
		}
		return CachingFileRepository.withTranslatedFileDescriptor()
		                            .expireOnEvict( true )
		                            .expireOnShutdown( true )
		                            .timeBasedExpiration( 10000, 0 )
		                            .targetFileRepository(
				                            AzureFileRepository.builder()
				                                               .blobClient( cloudBlobClient )
				                                               .containerName( "car-files" )
				                                               .repositoryId( "permanent" )
				                                               .build()
		                            )
		                            .cacheRepositoryId( "cache" )
		                            .build();
	}

	@Bean
	@Profile("aws")
	public AmazonS3 amazonS3( LocalStackContainer localStackContainer ) {
		return AmazonS3ClientBuilder.standard()
		                            .withEndpointConfiguration( localStackContainer.getEndpointConfiguration( S3 ) )
		                            .withPathStyleAccessEnabled( true )
		                            .withCredentials( localStackContainer.getDefaultCredentialsProvider() )
		                            .build();
	}

	@Bean
	@Profile("aws")
	public LocalStackContainer localStackContainer() {
		LocalStackContainer localstack = new LocalStackContainer( "0.9.3" )
				.withFileSystemBind( "../local-data/storage/localstack", "/tmp/localstack/data" )
				.withServices( S3 );
		localstack.start();
		return localstack;
	}

	@Bean
	@Profile("aws")
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize( 10 );
		taskExecutor.setQueueCapacity( 0 );
		taskExecutor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
		return taskExecutor;
	}

	@Bean
	@Profile("aws")
	public FileRepository fileRepository( AmazonS3 amazonS3, TaskExecutor taskExecutor ) {
		if ( !amazonS3.doesBucketExist( "car-files" ) ) {
			amazonS3.createBucket( "car-files" );
		}

		return CachingFileRepository.withTranslatedFileDescriptor()
		                            .expireOnEvict( true )
		                            .expireOnShutdown( true )
		                            .timeBasedExpiration( 10000, 0 )
		                            .targetFileRepository(
				                            AmazonS3FileRepository.builder()
				                                                  .repositoryId( "permanent" )
				                                                  .amazonS3( amazonS3 )
				                                                  .bucketName( "car-files" )
				                                                  .taskExecutor( taskExecutor )
				                                                  .build()
		                            )
		                            .cacheRepositoryId( "cache" )
		                            .build();
	}

	@Bean
	public FileRepository tempRepository() {
		return ExpiringFileRepository.builder()
		                             .targetFileRepository(
				                             LocalFileRepository.builder().repositoryId( FileManager.TEMP_REPOSITORY )
				                                                .rootFolder( "../local-data/storage/temp" )
				                                                .build()
		                             )
		                             .expireOnEvict( true )
		                             .expireOnShutdown( true )
		                             .build();
	}

	public static void main( String[] args ) {
		System.out.println( "--" );
		System.out.println( "Requires localstack or azurite to be running (docker-compose up)" );
		System.out.println( "--" );
		System.out.println( "" );
		SpringApplication springApplication = new SpringApplication( FileManagerTestApplication.class );
		springApplication.setDefaultProperties(
				Collections.singletonMap( "spring.config.additional-location", "${user.home}/dev-configs/fmm-test-application.yml" ) );
		springApplication.run( args );
	}
}