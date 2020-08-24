package utils;

import org.testcontainers.containers.GenericContainer;

import java.util.stream.IntStream;

public class FtpContainer extends GenericContainer<FtpContainer>
{
	public FtpContainer() {
		super( "stilliard/pure-ftpd" );
		addEnv( "FTP_USER_NAME", "fmm" );
		addEnv( "FTP_USER_PASS", "test" );
		addEnv( "FTP_USER_HOME", "/fmm/tests" );
		addEnv( "PUBLICHOST", "localhost" );
		addEnv( "ADDED_FLAGS", "-d -d" );

		addFixedExposedPort( 21, 21 );
		IntStream.iterate( 30000, value -> value + 1 ).limit( 10 )
		         .forEach(
				         value -> addFixedExposedPort( value, value )
		         );
	}
}
