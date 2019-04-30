# ImageServer

## Docker builds
Docker is required for running and building the source code.
When available, no local configuration changes or installation is required.

A Docker image with Ghostscript and GraphicsMagick is preconfigured.

### GraphicsMagick docker
All GraphicsMagick tasks (integration tests, test application) are executed inside the docker container called `imagemagick-container`.
The configuration properties are set to use the `gm` script in the root of the repository for GraphicsMagick commands.
This forwards to the Docker container when called outside of the docker container.
Inside the Docker container it forwards directly to the GraphicsMagick binary.

> You should ensure that the container is running if you want to execute integration tests. 
When using IntelliJ, this should be done automatically when opening the project.

### Maven command-line build

You can run a Maven build of the entire project using the provided `docker-compose`:

```docker-compose --no-ansi run --rm maven-gm mvn -U clean verify```

## IntelliJ integration
The IntelliJ project configuration (`.idea` folder) is included in the repository.
It contains shared run configurations for the docker container as well as for starting the test application.

The project contains a startup task which automatically starts the `imagemagick-container` Docker when opening the project.

## Test application 

Test web application that combines ImageServer API and Admin in a single web application.
You can easily deploy this webapplication, it will configure itself using an in-memory HSQLDB and the
temporary directory defined by java.io.tmpdir.

This webapplication is also used for running the integration tests.

### S3 support

The embedded test application features custom FileRepository configurations.
Enabling the AWS S3 file repositories is done by providing bucketname and region via the `aws.files.bucket` and `aws.files.region` configuration properties.
Authenticating against your aws account can be done by

* providing a default profile in your aws configuration 
    * `${USER_HOME}/.aws/credentials` - for credentials
    * `${USER_HOME}/.aws/config` - to specify a region
* providing environment variables:
    * AWS_ACCESS_KEY_ID
    * AWS_SECRET_KEY
    * (AWS_SESSION_TOKEN)

