package com.foreach.imageserver.test.embedded.application.extensions;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.foreach.across.core.annotations.ModuleConfiguration;
import com.foreach.across.core.annotations.OrderInModule;
import com.foreach.across.modules.filemanager.FileManagerModule;
import com.foreach.across.modules.filemanager.services.AwsS3FileRepository;
import com.foreach.across.modules.filemanager.services.DateFormatPathGenerator;
import com.foreach.across.modules.filemanager.services.FileManager;
import com.foreach.across.modules.filemanager.services.FileRepositoryRegistry;
import com.foreach.imageserver.core.config.ServicesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;

/**
 * @author Steven Gentens
 * @since 5.0.0
 */
@ModuleConfiguration(FileManagerModule.NAME)
@OrderInModule(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(value = "aws.files.bucket")
public class AwsFileRepositoryConfiguration
{
	@Value("${aws.files.bucket}")
	protected String bucket;
	@Value("${aws.files.region}")
	protected String region;

	@Autowired
	public void registerFileRepository( FileRepositoryRegistry fileRepositoryRegistry, FileManager fileManager ) {
		AWSCredentialsProviderChain providerChain = new AWSCredentialsProviderChain( new EnvironmentVariableCredentialsProvider(),
		                                                                             new ProfileCredentialsProvider( "default" ) );
		AWSCredentials awsCredentials = providerChain.getCredentials();

		AwsS3FileRepository variantsRepository = new AwsS3FileRepository( ServicesConfiguration.IMAGESERVER_VARIANTS_REPOSITORY, bucket,
		                                                                  awsCredentials.getAWSAccessKeyId(),
		                                                                  awsCredentials.getAWSSecretKey(),
		                                                                  fileManager, region, DateFormatPathGenerator.YEAR_MONTH_DAY );
		fileRepositoryRegistry.registerRepository( variantsRepository );
	}
}
