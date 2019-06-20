package lt;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.ImageTypeDto;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static lt.LTMethods.*;

/**
 * This test will:
 * - Start out by loading the same image file NR_OF_ORIGINALS times.
 * - Concurrently start NR_OF_ORIGINALS times NR_OF_READS_MULTIPLIER requests to generate NR_OF_ORIGINALS JPEG variants.
 * - Concurrently start NR_OF_ORIGINALS times NR_OF_READS_MULTIPLIER requests to generate NR_OF_ORIGINALS PNG variants.
 * - Verify that the same variant was generated each time.
 */
public class LTGenerateThroughput
{
	private static final Random RANDOM = new Random();
	private static final long VERY_BIG_LONG = -8520339600000l;

	private static final String ENDPOINT = "http://localhost:8078/resources/images";
	private static final String ACCESS_TOKEN = "standalone-access-token";
	private static final Date IMAGES_DATE = new Date( (long) ( RANDOM.nextDouble() * VERY_BIG_LONG ) );
	private static final String ID_PREFIX = ( new SimpleDateFormat( "yyyyMMdd" ) ).format( IMAGES_DATE );
	private static final int WIDTH = 640;
	private static final int HEIGHT = 480;
	private static final String CONTEXT = "website";

	private static final int NR_OF_ORIGINALS = 10;
	private static final int NR_OF_READS_MULTIPLIER = 20;
	private static final long BREAK_TIME = 5000;

	public static void main( String[] args ) throws Exception {
		System.out.println( String.format( "Using id prefix %s.", ID_PREFIX ) );

		ImageServerClient imageServerClient = new RemoteImageServerClient( ENDPOINT, ACCESS_TOKEN );

		List<String> imageIds = loadImages( imageServerClient, NR_OF_ORIGINALS, IMAGES_DATE, ID_PREFIX );
		pause( BREAK_TIME );

		readImagesConcurrently( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.JPEG,
		                        NR_OF_READS_MULTIPLIER, false );
		pause( BREAK_TIME );

		readImagesConcurrently( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.PNG,
		                        NR_OF_READS_MULTIPLIER, false );
		pause( BREAK_TIME );

		assertSameImages( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.JPEG );
		assertSameImages( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.PNG );

		System.out.println( "Done." );
	}

}
