package com.foreach.across.modules.filemanager.services;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpRemoteFileTemplate;
import utils.FtpContainer;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSpringIntegrationFtpFileRepository extends BaseFileRepositoryTest
{
	static FtpContainer ftpContainer = new FtpContainer();
	private static FtpRemoteFileTemplate template;

	@Override
	FileRepository createRepository() {
		if ( template == null ) {
			DefaultFtpSessionFactory defaultFtpSessionFactory = new DefaultFtpSessionFactory();
			defaultFtpSessionFactory.setClientMode( FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE );
			defaultFtpSessionFactory.setUsername( "fmm" );
			defaultFtpSessionFactory.setPassword( "test" );
			defaultFtpSessionFactory.setHost( "localhost" );
			defaultFtpSessionFactory.setPort( 21 );
			defaultFtpSessionFactory.setDefaultTimeout( 5000 );
			defaultFtpSessionFactory.setConnectTimeout( 5000 );
			defaultFtpSessionFactory.setDataTimeout( 5000 );

			template = new FtpRemoteFileTemplate( defaultFtpSessionFactory );
			// STAT seems to incorrectly return files as still existing
			template.setExistsMode( FtpRemoteFileTemplate.ExistsMode.NLST );
		}

		FileManager fileManager = mock( FileManager.class );
		when( fileManager.createTempFile() ).thenAnswer( invoc -> new File( tempDir, UUID.randomUUID().toString() ) );

		SpringIntegrationFtpFileRepository ftp = SpringIntegrationFtpFileRepository.builder()
		                                                                           .repositoryId( "ftp-repo" )
		                                                                           .remoteFileTemplate( template )
		                                                                           .build();
		ftp.setFileManager( fileManager );

		return ftp;
	}

	@BeforeAll
	static void init() throws InterruptedException {
		ftpContainer.start();
		// give container some time to instantiate
		Thread.sleep( 1000 );
	}

	@AfterAll
	static void tearDown() {
		ftpContainer.stop();
	}
}
