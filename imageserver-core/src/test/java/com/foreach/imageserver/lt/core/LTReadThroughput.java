package com.foreach.imageserver.lt.core;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.client.RemoteImageServerClient;
import com.foreach.imageserver.dto.ImageTypeDto;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.foreach.imageserver.lt.core.LTMethods.*;

/**
 * This test will:
 * - Start out by loading the same image file NR_OF_ORIGINALS times.
 * - Generate a JPEG and a PNG variant for every original.
 * - Use NR_OF_THREADS threads to read NR_OF_ORIGINALS * NR_OF_REQUESTS_MULTIPLIER * <nr_of_image_types> variants in total.
 */
public class LTReadThroughput
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
	private static final int NR_OF_THREADS = 10;
	private static final int NR_OF_REQUESTS_MULTIPLIER = 100;
	private static final long BREAK_TIME = 5000;

	public static void main( String[] args ) throws Exception {
		System.out.println( String.format( "Using id prefix %s.", ID_PREFIX ) );

		ImageServerClient imageServerClient = new RemoteImageServerClient( ENDPOINT, ACCESS_TOKEN );

		List<String> imageIds = loadImages( imageServerClient, NR_OF_ORIGINALS, IMAGES_DATE, ID_PREFIX );
		readImagesSequentially( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.JPEG );
		readImagesSequentially( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.PNG );
		assertSameImages( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.JPEG );
		assertSameImages( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, ImageTypeDto.PNG );
		pause( BREAK_TIME );

		readNumberOfImagesConcurrently( imageServerClient, imageIds, CONTEXT, WIDTH, HEIGHT, Arrays.asList(
				ImageTypeDto.JPEG, ImageTypeDto.PNG ), NR_OF_THREADS, NR_OF_REQUESTS_MULTIPLIER );

		System.out.println( "Done." );
	}

}
