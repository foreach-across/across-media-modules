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
import com.foreach.across.modules.filemanager.services.AmazonS3FileRepository;
import com.foreach.across.modules.filemanager.services.CachingFileRepository;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.hibernate.jpa.AcrossHibernateJpaModule;
import com.foreach.across.modules.properties.PropertiesModule;
import com.foreach.across.modules.web.AcrossWebModule;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;

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
	public FileRepository fileRepository( AmazonS3 amazonS3 ) {
		if ( !amazonS3.doesBucketExist( "car-files" ) ) {
			amazonS3.createBucket( "car-files" );
		}

		return CachingFileRepository.withTranslatedFileDescriptor()
		                            .removeCacheOnEvict( true )
		                            .removeCacheOnShutdown( true )
		                            .timeBasedRemoval( 10000, 0 )
		                            .targetFileRepository(
				                            AmazonS3FileRepository.builder()
				                                                  .repositoryId( "permanent" )
				                                                  .amazonS3( amazonS3 )
				                                                  .bucketName( "car-files" )
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
