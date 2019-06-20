package com.foreach.across.samples.filemanager;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
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
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Steven Gentens
 */
@AcrossApplication(modules = { AdminWebModule.NAME, FileManagerModule.NAME, AcrossWebModule.NAME, AcrossHibernateJpaModule.NAME, PropertiesModule.NAME,
                               EntityModule.NAME })
public class FileManagerTestApplication
{
	@Bean
	public AmazonS3 amazonS3() {
		return AmazonS3ClientBuilder.standard()
		                            .withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration( "http://localhost:4572", "us-east-1" ) )
		                            .withPathStyleAccessEnabled( true )
		                            .withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( "test", "test" ) ) )
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

	@Bean
	public TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setMaxPoolSize( 10 );
		taskExecutor.setQueueCapacity( 0 );
		taskExecutor.setRejectedExecutionHandler( new ThreadPoolExecutor.CallerRunsPolicy() );
		return taskExecutor;
	}

	@Bean
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

	public static void main( String[] args ) {
		System.out.println( "--" );
		System.out.println( "Requires localstack to be running (docker-compose up)" );
		System.out.println( "--" );
		System.out.println( "" );
		SpringApplication springApplication = new SpringApplication( FileManagerTestApplication.class );
		springApplication.setDefaultProperties( Collections.singletonMap( "spring.config.location", "${user.home}/dev-configs/fmm-test-application.yml" ) );
		springApplication.run( args );
	}

}
