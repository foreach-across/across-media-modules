package utils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * Helper methods for localstack Amazon S3 client.
 *
 * @author Arne Vandamme
 * @since 1.4.0
 */
@UtilityClass
public class AmazonS3Helper
{
	public static AmazonS3 createClientWithBuckets( String... bucketsToCreate ) {
		AmazonS3 amazonS3 = AmazonS3ClientBuilder
				.standard()
				.withEndpointConfiguration( new AwsClientBuilder.EndpointConfiguration( "http://localhost:4572", "us-east-1" ) )
				.withPathStyleAccessEnabled( true )
				.withCredentials( new AWSStaticCredentialsProvider( new BasicAWSCredentials( "test", "test" ) ) )
				.build();

		Stream.of( bucketsToCreate )
		      .forEach( bucketName -> {
			      if ( !amazonS3.doesBucketExist( bucketName ) ) {
				      amazonS3.createBucket( bucketName );
			      }
		      } );

		return amazonS3;
	}

	public static void deleteBuckets( AmazonS3 amazonS3, String... bucketsToDelete ) {
		Stream.of( bucketsToDelete )
		      .forEach( bucketName -> {
			      if ( amazonS3.doesBucketExist( bucketName ) ) {
				      amazonS3.listObjects( bucketName ).getObjectSummaries().forEach( o -> amazonS3.deleteObject( bucketName, o.getKey() ) );
				      amazonS3.deleteBucket( bucketName );
			      }
		      } );
	}

	public static void createFolder( AmazonS3 amazonS3, String bucketName, String folderName ) {
		// create meta-data for your folder and set content-length to 0
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength( 0 );
		// create empty content
		InputStream emptyContent = new ByteArrayInputStream( new byte[0] );
		// create a PutObjectRequest passing the folder name suffixed by /
		PutObjectRequest putObjectRequest = new PutObjectRequest(
				bucketName, folderName + ( StringUtils.endsWith( folderName, "/" ) ? "" : "/" ), emptyContent, metadata
		);
		// send request to S3 to create folder
		amazonS3.putObject( putObjectRequest );
	}

}