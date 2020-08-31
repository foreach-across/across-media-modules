package utils;

import com.github.dockerjava.api.model.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.util.List;
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

		IntStream.iterate( 30000, value -> value + 1 ).limit( 10 )
		         .forEach(
				         value -> addFixedExposedPort( value, value )
		         );

		waitingFor( Wait.forLogMessage( "(\\s)*(pure-ftpd  -l puredb:\\/etc\\/pure-ftpd\\/pureftpd.pdb).*", 1 ) );
	}

	@Override
	public void stop() {
		super.stop();
		boolean wait = true;
		while ( wait ) {
			List<Container> exec = getDockerClient().listContainersCmd().exec();
			wait = exec.stream().anyMatch( c -> c.getImage().contains( "pure-ftpd" ) );
		}

	}
}
