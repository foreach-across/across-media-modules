## FileManagerModule
Please refer to the [module page][] for all information regarding documentation, issue tracking and support.

### Contributing
Contributions in the form of pull requests are greatly appreciated.  Please refer to the [contributor guidelines][] for more details. 

### Building from source
The source can be built using [Maven][] with JDK 8.

### Local development

Some tests are AWS specific, they require [Localstack][] with the S3 service to be running.
Some tests are Azure specific, they require [Azurite][] with the blob service to be runnning.
A `docker-compose` file is available in the root of this repository, running `docker-compose up` should start the required AWS and Azure services with data being stored in `local-data/storage/localstack` & `local-data/storage/azurite`.

 > On macOS you might have to do `TMPDIR=/private$TMPDIR docker-compose up` to workaround tmp dir issues.

In order to start the test application in development mode with support for one of the cloud providers an additional profile is required, `azure` for azure support and `aws` for aws support.

### License
Licensed under version 2.0 of the [Apache License][].

[module page]: https://across.dev/modules/filemanagermodule
[contributor guidelines]: https://across.dev/contributing
[Maven]: http://maven.apache.org
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[Localstack]: https://github.com/localstack/localstack
[Azurite]: https://github.com/Azure/Azurite
