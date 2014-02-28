package com.foreach.imageserver.connectors.dpp;

import com.foreach.imageserver.connectors.dpp.ws.HttpWebServiceInvoker;
import com.foreach.imageserver.connectors.dpp.ws.HttpWebServiceRequest;
import com.foreach.imageserver.connectors.dpp.ws.WebServiceResult;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author niels
 * @since 26/02/13
 */
public class AssetConversionServiceInvoker extends HttpWebServiceInvoker
{
	private AssetConversionServiceInvoker( String baseurl ) {
		super( baseurl, AssetConversionService.RESPONSE_ENCODING );
		modifyThreadPoolSize( 2, 5 );
	}

	@Override
	public WebServiceResult getResultWithStream( HttpWebServiceRequest request, InputStream is, int responseCode ) {
		try {
			byte[] result = IOUtils.toByteArray( is );
			return new AssetConversionServiceResponse( result );
		}
		catch ( IOException e ) {
			throw new RuntimeException( "Failed to read the webservice response", e );
		}
	}

	@Override
	public WebServiceResult invoke( HttpWebServiceRequest httpRequest ) {
		AssetConversionServiceRequest request = (AssetConversionServiceRequest) httpRequest;
		HttpClient httpClient = new HttpClient();

		try {
			PostMethod filePost = new PostMethod( baseurl + request.getExtraParams() );
			Part[] parts = { new FilePart( "asset", request.getFileToConvert() ) };
			filePost.setRequestEntity( new MultipartRequestEntity( parts, filePost.getParams() ) );
			int responseCode = httpClient.executeMethod( filePost );
			return getResultWithStream( request, filePost.getResponseBodyAsStream(), responseCode );
		}
		catch ( IOException e ) {
			throw new RuntimeException( "Failed to add asset to the request", e );
		}
	}

	public static class PdfAssetConversionServiceInvoker extends AssetConversionServiceInvoker
	{

		public PdfAssetConversionServiceInvoker() {
			super( AssetConversionService.PDF_CONVERSION_URL );
		}

	}

	public static class ImageAssetConversionServiceInvoker extends AssetConversionServiceInvoker
	{
		public ImageAssetConversionServiceInvoker() {
			super( AssetConversionService.IMG_CONVERSION_URL );
		}
	}
}
