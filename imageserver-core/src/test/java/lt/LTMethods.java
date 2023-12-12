package lt;

import com.foreach.imageserver.client.ImageServerClient;
import com.foreach.imageserver.dto.ImageTypeDto;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.StopWatch;
import support.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class LTMethods
{
	public static List<String> loadImages( ImageServerClient imageServerClient,
	                                       int nrOfOriginals,
	                                       Date dateForImages,
	                                       String idPrefix ) throws Exception {
		System.out.println( String.format( "Loading %d original images.", nrOfOriginals ) );

		byte[] imageData;
		try (InputStream imageStream = LTReadThroughput.class.getClassLoader().getResourceAsStream(
				"images/loadTest.jpg" )) {
			imageData = IOUtils.toByteArray( imageStream );
		}

		StopWatch totalTimer = new StopWatch();
		List<String> imageIds = new ArrayList<>( nrOfOriginals );
		SortedMap<String, StopWatch> taskTimers = new TreeMap<>();
		for ( int i = 0; i < nrOfOriginals; ++i ) {
			String id = idPrefix + "-" + i;
			imageIds.add( id );
			taskTimers.put( id, new StopWatch() );
		}

		totalTimer.start();
		for ( String id : imageIds ) {
			StopWatch taskTimer = taskTimers.get( id );
			taskTimer.start();
			imageServerClient.loadImage( id, imageData, dateForImages );
			taskTimer.stop();
		}
		totalTimer.stop();

		System.out.println( prettyPrintTimings( totalTimer, taskTimers ) );

		return imageIds;
	}

	public static void readImagesSequentially( ImageServerClient imageServerClient,
	                                           List<String> imageIds,
	                                           String context, int width,
	                                           int height, ImageTypeDto imageType ) {
		System.out.println(
				String.format( "Reading %d %s variants sequentially.", imageIds.size(), imageType.toString() ) );

		StopWatch totalTimer = new StopWatch();
		SortedMap<String, StopWatch> taskTimers = new TreeMap<>();
		for ( String id : imageIds ) {
			taskTimers.put( id, new StopWatch() );
		}

		totalTimer.start();
		for ( String id : imageIds ) {
			StopWatch taskTimer = taskTimers.get( id );
			taskTimer.start();
			imageServerClient.imageStream( id, context, width, height, imageType );
			taskTimer.stop();
		}
		totalTimer.stop();

		System.out.println( prettyPrintTimings( totalTimer, taskTimers ) );
	}

	public static void readImagesConcurrently( ImageServerClient imageServerClient,
	                                           List<String> imageIds,
	                                           String context,
	                                           int width,
	                                           int height,
	                                           ImageTypeDto imageType,
	                                           int nrOfReadsMultiplier, boolean consumeReads ) throws Exception {
		int nrOfReads = imageIds.size() * nrOfReadsMultiplier;

		System.out.println( String.format( "Reading %d %s variants concurrently.", nrOfReads, imageType.toString() ) );

		StopWatch totalTimer = new StopWatch();
		SortedMap<String, StopWatch> taskTimers = new TreeMap<>();
		SortedMap<String, AtomicBoolean> errorFlags = new TreeMap<>();
		Set<Thread> threads = new HashSet<>( nrOfReads );
		CyclicBarrier barrier = new CyclicBarrier( nrOfReads + 1 );

		for ( int i = 0; i < nrOfReadsMultiplier; ++i ) {
			for ( String id : imageIds ) {
				StopWatch taskTimer = new StopWatch();
				AtomicBoolean errorFlag = new AtomicBoolean( false );
				Runnable task =
						new ConsumeImageStreamTask( taskTimer, imageServerClient, id, context, width, height, imageType,
						                            errorFlag, consumeReads, barrier );
				Thread thread = new Thread( task );
				thread.start();
				taskTimers.put( id + "-" + i, taskTimer );
				errorFlags.put( id + "-" + i, errorFlag );
				threads.add( thread );
			}
		}

		while ( barrier.getNumberWaiting() != nrOfReads ) {
			Thread.sleep( 1000 );
		}

		totalTimer.start();
		barrier.await();
		for ( Thread thread : threads ) {
			thread.join();
		}
		totalTimer.stop();

		if ( barrier.isBroken() || barrier.getNumberWaiting() != 0 ) {
			throw new Exception( "Unexpected barrier state." );
		}

		System.out.println( prettyPrintTimings( totalTimer, taskTimers ) );
		System.out.println( prettyErrorFlags( errorFlags ) );
	}

	public static void readNumberOfImagesConcurrently( ImageServerClient imageServerClient,
	                                                   List<String> imageIds,
	                                                   String context,
	                                                   int width,
	                                                   int height,
	                                                   List<ImageTypeDto> types,
	                                                   int nrOfThreads,
	                                                   int nrOfRequestsMultiplier ) throws Exception {
		int nrOfRequests = nrOfRequestsMultiplier * imageIds.size() * types.size();
		System.out.println( String.format( "Reading %s variants using %d threads.", nrOfRequests, nrOfThreads ) );

		StopWatch totalTimer = new StopWatch();
		SortedMap<String, StopWatch> taskTimers = new TreeMap<>();
		SortedMap<String, AtomicBoolean> errorFlags = new TreeMap<>();
		List<Runnable> tasks = new ArrayList<>( nrOfRequests );
		for ( int i = 0; i < nrOfRequestsMultiplier; ++i ) {
			for ( String id : imageIds ) {
				for ( ImageTypeDto imageType : types ) {
					StopWatch taskTimer = new StopWatch();
					AtomicBoolean errorFlag = new AtomicBoolean( false );
					tasks.add( new ConsumeImageStreamTask( taskTimer, imageServerClient, id, context, width, height,
					                                       imageType, errorFlag, true, null ) );
					taskTimers.put( id + "-" + i + "-" + imageType.toString(), taskTimer );
					errorFlags.put( id + "-" + i + "-" + imageType.toString(), errorFlag );
				}
			}
		}

		ExecutorService executorService = Executors.newFixedThreadPool( nrOfThreads );
		totalTimer.start();
		for ( Runnable task : tasks ) {
			executorService.submit( task );
		}
		executorService.shutdown();
		executorService.awaitTermination( Long.MAX_VALUE, TimeUnit.DAYS );
		totalTimer.stop();

		System.out.println( prettyPrintSummary( totalTimer, taskTimers ) );
		System.out.println( prettyErrorFlags( errorFlags ) );
	}

	public static void assertSameImages( ImageServerClient imageServerClient,
	                                     List<String> imageIds,
	                                     String context, int width,
	                                     int height, ImageTypeDto imageType ) throws Exception {
		System.out.println( String.format( "Comparing %s variants.", imageType.toString() ) );

		boolean encounteredIssue = false;

		BufferedImage referenceImage = null;
		String referenceId = null;
		for ( String id : imageIds ) {
			try (InputStream imageStream = imageServerClient.imageStream( id, context, width, height, imageType )) {
				BufferedImage image = ImageIO.read( imageStream );
				if ( referenceImage == null ) {
					referenceImage = image;
					referenceId = id;
				}
				else if ( !ImageUtils.imagesAreEqual( referenceImage, image ) ) {
					System.out.println( String.format( "Image mismatch for ids %s and %s.", referenceId, id ) );
					encounteredIssue = true;
				}
			}
		}

		if ( encounteredIssue ) {
			throw new Exception( "Image verification failed." );
		}
	}

	public static void pause( long breakTime ) throws Exception {
		System.out.println( String.format( "Pausing for %d milliseconds.", breakTime ) );
		Thread.sleep( breakTime );
	}

	public static String prettyPrintTimings( StopWatch totalTimer, SortedMap<String, StopWatch> taskTimers ) {
		StringBuffer sb = new StringBuffer();
		sb.append( prettyPrintSummary( totalTimer, taskTimers ) );

		for ( SortedMap.Entry<String, StopWatch> taskTimer : taskTimers.entrySet() ) {
			String taskName = taskTimer.getKey();
			StopWatch stopWatch = taskTimer.getValue();
			sb.append( String.format( "%-25s%10dms\n", taskName, stopWatch.getTime() ) );
		}

		return sb.toString();
	}

	public static String prettyPrintSummary( StopWatch totalTimer, SortedMap<String, StopWatch> taskTimers ) {
		double averageTaskTime = computeAverageTaskTime( taskTimers );
		double standardDeviation = Math.sqrt( computeVariance( taskTimers, averageTaskTime ) );

		StringBuffer sb = new StringBuffer();
		sb.append( String.format( "Total: %dms, Average: %dms, Standard Deviation: %dms.\n", totalTimer.getTime(),
		                          (long) averageTaskTime, (long) standardDeviation ) );
		return sb.toString();
	}

	public static String prettyErrorFlags( SortedMap<String, AtomicBoolean> errorFlags ) {
		List<String> idsWithErrors = extractFlaggedIds( errorFlags );

		StringBuffer sb = new StringBuffer();
		sb.append( String.format( "Encountered reading errors for %d images.", idsWithErrors.size() ) );

		for ( String id : idsWithErrors ) {
			sb.append( id );
		}

		return sb.toString();
	}

	public static List<String> extractFlaggedIds( SortedMap<String, AtomicBoolean> errorFlags ) {
		List<String> flaggedIds = new ArrayList<>();
		for ( SortedMap.Entry<String, AtomicBoolean> errorFlag : errorFlags.entrySet() ) {
			if ( errorFlag.getValue().get() ) {
				flaggedIds.add( errorFlag.getKey() );
			}
		}
		return flaggedIds;
	}

	public static double computeAverageTaskTime( SortedMap<String, StopWatch> taskTimers ) {
		Collection<StopWatch> timers = taskTimers.values();
		long sum = 0;
		for ( StopWatch taskTimer : timers ) {
			sum += taskTimer.getTime();
		}
		return (double) sum / (double) timers.size();
	}

	private static double computeVariance( SortedMap<String, StopWatch> taskTimers, double averageTaskTime ) {
		Collection<StopWatch> timers = taskTimers.values();
		long sum = 0;
		for ( StopWatch taskTimer : timers ) {
			sum += Math.pow( ( taskTimer.getTime() - averageTaskTime ), 2 );
		}
		return (double) sum / (double) timers.size();
	}

	public static class ConsumeImageStreamTask implements Runnable
	{
		private final StopWatch taskTimer;
		private final ImageServerClient imageServerClient;
		private final String id;
		private final String context;
		private final int width;
		private final int height;
		private final ImageTypeDto imageType;
		private final AtomicBoolean errorFlag;
		private final boolean consumeReads;
		private final CyclicBarrier barrier;

		public ConsumeImageStreamTask(
				StopWatch taskTimer,
				ImageServerClient imageServerClient,
				String id,
				String context,
				int width,
				int height,
				ImageTypeDto imageType, AtomicBoolean errorFlag, boolean consumeReads, CyclicBarrier barrier ) {
			this.taskTimer = taskTimer;
			this.imageServerClient = imageServerClient;
			this.id = id;
			this.context = context;
			this.width = width;
			this.height = height;
			this.imageType = imageType;
			this.errorFlag = errorFlag;
			this.consumeReads = consumeReads;
			this.barrier = barrier;
		}

		@Override
		public void run() {
			try {
				if ( barrier != null ) {
					barrier.await();
				}

				taskTimer.start();
				try (InputStream imageStream = imageServerClient.imageStream( id, context, width, height, imageType )) {
					if ( consumeReads ) {
						// Make sure we consume the stream.
						while ( imageStream.read() != -1 );
					}
				}
			}
			catch ( Exception e ) {
				errorFlag.set( true );
			}

			taskTimer.stop();
		}
	}
}
