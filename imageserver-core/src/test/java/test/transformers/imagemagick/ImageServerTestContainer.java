package test.transformers.imagemagick;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.images.builder.ImageFromDockerfile;

/***
 * This utility class can be used to create the imageserver-container.
 *
 * We might want to ship this as a separate dependency sometime so other projects can reuse and/or extend this container.
 */
public interface ImageServerTestContainer
{
	GenericContainer CONTAINER = new GenericContainer<>(
			new ImageFromDockerfile()
					.withDockerfileFromBuilder( builder ->
							                            builder
									                            .from( "maven:3.8.7-eclipse-temurin-8" )
									                            .run( "apt-get update && apt-get install -y ghostscript graphicsmagick" )
									                            .cmd( "tail -f /dev/null" )
									                            .build() ) )
			.withCreateContainerCmdModifier( createContainerCmd -> {
				createContainerCmd.withHostName( "imageserver-container" );
				createContainerCmd.withName( "imageserver-container" );
			} );
}
