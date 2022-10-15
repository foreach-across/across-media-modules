package utils;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.Authentication;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.impl.AbstractUserManager;
import org.apache.ftpserver.usermanager.impl.BaseUser;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FtpContainer
{
	public static final int TEST_PORT = 10021;
	private final Path tempDir;
	private final FtpServer server;

	@SneakyThrows
	public FtpContainer() {
		tempDir = Files.createTempDirectory( "fmm-ftp" );
		FtpServerFactory serverFactory = new FtpServerFactory();
		serverFactory.setUserManager( new InMemoryUserManager( tempDir.toFile() ) );
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort( TEST_PORT );
		Map<String, Listener> listeners = new HashMap<>(); // must be mutable for tearDown()
		listeners.put( "default", listenerFactory.createListener() );
		serverFactory.setListeners( listeners );
		server = serverFactory.createServer();
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

	public static final class InMemoryUserManager extends AbstractUserManager
	{

		public static final BaseUser baseUser = new BaseUser();

		public InMemoryUserManager( File tempDir ) {
			baseUser.setName( "fmm" );
			baseUser.setEnabled( true );
			baseUser.setHomeDirectory( tempDir.getAbsolutePath() );
			baseUser.setPassword( "password" );

			baseUser.setAuthorities( Arrays.asList( new WritePermission(),
			                                        new ConcurrentLoginPermission( 0, 0 ) ) );
		}

		@Override
		public User getUserByName( String username ) {
			return baseUser;
		}

		@Override
		public String[] getAllUserNames() {
			return new String[] { baseUser.getName() };
		}

		@Override
		public void delete( String username ) {
			throw new RuntimeException( "not implemented" );
		}

		@Override
		public void save( User user ) {
			throw new RuntimeException( "not implemented" );
		}

		@Override
		public boolean doesExist( String username ) {
			return username.equals( baseUser.getName() );
		}

		@Override
		public User authenticate( Authentication authentication ) {
			return baseUser;
		}
	}
}
