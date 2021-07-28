package utils;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;

public class SftpContainer
{
	private final Path tempDir;
	private final SshServer server;

	@SneakyThrows
	public SftpContainer() {
		tempDir = Files.createTempDirectory( "fmm-ftp" );
		server = SshServer.setUpDefaultServer();
		server.setPort( 22 );
		server.setKeyPairProvider( new SimpleGeneratorHostKeyProvider() );
		server.setUserAuthFactories( Arrays.asList( new UserAuthPasswordFactory() ) );
		server.setPasswordAuthenticator( new SftpAuthenticator() );
		SftpSubsystemFactory factory = new SftpSubsystemFactory.Builder()

//				.withFileSystemAccessor( new SftpFileSystemAccessor()
//				{
//					@Override
//					public SeekableByteChannel openFile( ServerSession session,
//					                                     SftpEventListenerManager subsystem,
//					                                     Path file,
//					                                     String handle,
//					                                     Set<? extends OpenOption> options,
//					                                     FileAttribute<?>... attrs ) throws IOException {
//						return null;
//					}
//
//					@Override
//					public FileLock tryLock( ServerSession session,
//					                         SftpEventListenerManager subsystem,
//					                         Path file,
//					                         String handle,
//					                         Channel channel,
//					                         long position,
//					                         long size,
//					                         boolean shared ) throws IOException {
//						return null;
//					}
//
//					@Override
//					public void syncFileData( ServerSession session,
//					                          SftpEventListenerManager subsystem,
//					                          Path file,
//					                          String handle,
//					                          Channel channel ) throws IOException {
//
//					}
//
//					@Override
//					public DirectoryStream<Path> openDirectory( ServerSession session,
//					                                            SftpEventListenerManager subsystem,
//					                                            Path dir,
//					                                            String handle ) throws IOException {
//						return null;
//					}
//				} )
.build();
		VirtualFileSystemFactory fileSystemFactory = new VirtualFileSystemFactory();
		fileSystemFactory.setDefaultHomeDir( tempDir );
		server.setFileSystemFactory( fileSystemFactory );
		server.setSubsystemFactories( Collections.singletonList( factory ) );
	}

	@SneakyThrows
	public void start() {
		server.start();
	}

	@SneakyThrows
	public void stop() {
		server.stop();
		FileUtils.forceDelete( tempDir.toFile() );
	}

	public class SftpAuthenticator implements PasswordAuthenticator
	{
		public SftpAuthenticator() {
		}

		@Override
		public boolean authenticate( String username, String password, ServerSession session ) throws PasswordChangeRequiredException, AsyncAuthException {
			boolean matchesUsername = "fmm".equals( username );
			boolean matchesPassword = "test".equals( password );
			return matchesUsername && matchesPassword;
		}
	}
}
