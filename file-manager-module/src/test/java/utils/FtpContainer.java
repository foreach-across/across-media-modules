package utils;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

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
		addEnv( "FTP_PASSIVE_PORTS", "15000:15009" );

		addFixedExposedPort( 21, 21 );
		IntStream.iterate( 15000, value -> value + 1 ).limit( 10 )
		         .forEach(
				         value -> {
					         addExposedPorts( value );
					         addFixedExposedPort( value, value );
				         }
		         );

		int[] defaultPorts = IntStream.iterate( 30000, value -> value + 1 ).limit( 10 )
		                              .toArray();
		addExposedPorts( defaultPorts );

		waitingFor( Wait.forLogMessage( "(\\s)*(pure-ftpd  -l puredb:\\/etc\\/pure-ftpd\\/pureftpd.pdb).*", 1 ) );
	}
}
