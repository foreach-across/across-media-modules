package com.foreach.across.modules.taskrunner.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foreach.across.modules.taskrunner.business.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Service
public class FileManagerServiceImpl implements FileManagerService
{
	private static final Logger LOG = LoggerFactory.getLogger( FileManagerService.class );

	// Todo: get rid of json dependency (?)
	private ObjectMapper jsonObjectMapper;

	public FileManagerServiceImpl() {
		jsonObjectMapper = new ObjectMapper();
	}

	private Path serialize( Object object, ReportFormat format ) throws IOException, IllegalArgumentException {
		if( format != null ) {
			switch ( format ) {
				case JSON:
					Path jsonFile = Files.createTempFile( null, ".json" );

					OutputStream out =
							Files.newOutputStream( jsonFile, StandardOpenOption.CREATE, StandardOpenOption.APPEND,
							                       StandardOpenOption.WRITE );
					jsonObjectMapper.writeValue( out, object );
					out.close();

					return jsonFile;
				default:
					throw new IllegalArgumentException( "unsupported report format" );
			}
		} else {
			throw new IllegalArgumentException( "report format not specified" );
		}
	}

	@Override
	public ReportResult getReportResult( Object object, ReportFormat format ) throws IOException {
		if( format != null ) {
			Path resultingPath = serialize( object, format );
			ReportResult reportResult = new ReportResult();

			switch ( format ) {
				case JSON:
					reportResult.addReportFileResult( new JsonReportFileResult( "foobar", resultingPath.toString() ) );
			}

			return reportResult;
		} else {
			throw new IllegalArgumentException( "report format not specified" );
		}
	}

	@Override
	public InputStream readReportResult( ReportFileResult reportFileResult, ReportTask reportTask ) {
		try {
			return Files.newInputStream( Paths.get( reportFileResult.getPath() ) );
		}
		catch ( IOException e ) {
			LOG.error( "Couldn't parse ReportTask {} result.", reportTask.getUuid(), e );
			return null;
		}
	}
}
