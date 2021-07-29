package com.foreach.across.samples.filemanager.application.ftp;

import com.foreach.across.modules.filemanager.services.CachingFileRepository;
import com.foreach.across.modules.filemanager.services.DateFormatPathGenerator;
import com.foreach.across.modules.filemanager.services.FileRepository;
import com.foreach.across.modules.filemanager.services.SpringIntegrationSftpFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpRemoteFileTemplate;

@Configuration
@Profile("sftp")
public class SftpRepositoryConfiguration
{
	@Bean
	DefaultSftpSessionFactory defaultFtpSessionFactory(
			@Value("${sftp1.username}") String username,
			@Value("${sftp1.password}") String pw,
			@Value("${sftp1.host}") String host,
			@Value("${sftp1.port}") int port ) {
		DefaultSftpSessionFactory defaultFtpSessionFactory = new DefaultSftpSessionFactory();
		defaultFtpSessionFactory.setAllowUnknownKeys( true );
		defaultFtpSessionFactory.setPassword( pw );
		defaultFtpSessionFactory.setUser( username );
		defaultFtpSessionFactory.setHost( host );
		defaultFtpSessionFactory.setPort( port );
		return defaultFtpSessionFactory;
	}

	@Bean
	SftpRemoteFileTemplate ftpRemoteFileTemplate( DefaultSftpSessionFactory dsf ) {
		return new SftpRemoteFileTemplate( dsf );
	}

	@Bean
	public FileRepository fileRepositoryAzure( SftpRemoteFileTemplate ftpRemoteFileTemplate ) {
		return CachingFileRepository.withTranslatedFileDescriptor()
		                            .expireOnEvict( true )
		                            .expireOnShutdown( true )
		                            .timeBasedExpiration( 10000, 0 )
		                            .targetFileRepository(
				                            SpringIntegrationSftpFileRepository.builder()
				                                                               .remoteFileTemplate( ftpRemoteFileTemplate )
				                                                               .repositoryId( "permanent" )
				                                                               .pathGenerator( new DateFormatPathGenerator( "yyyy/MM/dd" ) )
				                                                               .build()
		                            )
		                            .cacheRepositoryId( "cache" )
		                            .build();
	}
}
