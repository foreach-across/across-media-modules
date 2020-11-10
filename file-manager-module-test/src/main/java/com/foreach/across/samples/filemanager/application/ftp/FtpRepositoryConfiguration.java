package com.foreach.across.samples.filemanager.application.ftp;

import com.foreach.across.modules.filemanager.services.CachingFileRepository;
import com.foreach.across.modules.filemanager.services.DateFormatPathGenerator;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.SpringIntegrationFtpFileRepository;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;

@Configuration
@Profile("ftp")
public class FtpRepositoryConfiguration
{
	@Bean
	DefaultFtpSessionFactory defaultFtpSessionFactory(
			@Value("${ftp1.username}") String username,
			@Value("${ftp1.password}") String pw,
			@Value("${ftp1.host}") String host,
			@Value("${ftp1.port}") int port ) {
		DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
		defaultFtpSessionFactory.setClientMode( FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE );
		defaultFtpSessionFactory.setPassword( pw );
		defaultFtpSessionFactory.setUsername( username );
		defaultFtpSessionFactory.setHost( host );
		defaultFtpSessionFactory.setPort( port );
		return defaultFtpSessionFactory;
	}

	@Bean
	FtpRemoteFileTemplate ftpRemoteFileTemplate( DefaultFtpSessionFactory dsf ) {
		return new FtpRemoteFileTemplate( dsf );
	}

	@Bean
	public FileRepository fileRepositoryAzure( FtpRemoteFileTemplate ftpRemoteFileTemplate ) {
		return CachingFileRepository.withTranslatedFileDescriptor()
		                            .expireOnEvict( true )
		                            .expireOnShutdown( true )
		                            .timeBasedExpiration( 10000, 0 )
		                            .targetFileRepository(
				                            SpringIntegrationFtpFileRepository.builder()
				                                                              .remoteFileTemplate( ftpRemoteFileTemplate )
				                                                              .repositoryId( "permanent" )
				                                                              .pathGenerator( new DateFormatPathGenerator( "yyyy/MM/dd" ) )
				                                                              .build()
		                            )
		                            .cacheRepositoryId( "cache" )
		                            .build();
	}
}
