package com.foreach.imageserver.services.repositories;

import be.persgroep.red.diocontent.api.attachment.Attachment;
import be.persgroep.red.diocontent.api.attachment.AttachmentRole;
import be.persgroep.red.diocontent.api.client.DioContentClient;
import be.persgroep.red.diocontent.webservice.client.DefaultRestDioContentClient;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.exceptions.RepositoryLookupException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class DioContentLookupRepository implements ImageLookupRepository
{
	private static final Logger LOG = LoggerFactory.getLogger( DioContentLookupRepository.class );

	private final String serverUrl = "http://test.diocontentwebservice.persgroep.be.persgroep.be/v2.8/rest";
	private final String login = "_mfnapitest";
	private final String password = "mfnapitest";

	@Override
	public RepositoryLookupResult fetchImage( String uri ) {

		try {
			DioContentClient client = new DefaultRestDioContentClient( serverUrl, login, password );
			int dcId = Integer.valueOf( StringUtils.replace( uri, "dc:", "" ) );

			Attachment attachment = client.getAttachmentWithRole( dcId, AttachmentRole.ORIGINAL );
			ImageType imageType = ImageType.getForContentType( attachment.getFileInfo().getMimeType() );

			ByteArrayOutputStream data = new ByteArrayOutputStream();
			client.downloadAttachment( attachment.getId(), data );

			data.flush();
			IOUtils.closeQuietly( data );

			RepositoryLookupResult lookupResult = new RepositoryLookupResult();
			lookupResult.setStatus( RepositoryLookupStatus.SUCCESS );
			lookupResult.setImageType( imageType );
			lookupResult.setContent( new ByteArrayInputStream( data.toByteArray() ) );

			return lookupResult;
		}
		catch ( Exception e ) {
			throw new RepositoryLookupException( e );
		}
	}
}
