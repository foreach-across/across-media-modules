Test web application that combines ImageServer API and Admin in a single web application.
You can easily deploy this webapplication, it will configure itself using an in-memory HSQLDB and the
temporary directory defined by java.io.tmpdir.

This webapplication is also used for running the integration tests.

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

