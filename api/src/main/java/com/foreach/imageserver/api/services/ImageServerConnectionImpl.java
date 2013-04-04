package com.foreach.imageserver.api.services;

import com.foreach.imageserver.api.business.UploadStatus;
import com.foreach.imageserver.api.models.ImageModel;
import com.foreach.imageserver.api.models.ImageServerUploadResult;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.List;

/**
 * An implementation for the {@link ImageServerConnection} interface
 */

@ThreadSafe
public class ImageServerConnectionImpl extends HttpConnection implements ImageServerConnection
{
	private static final Logger LOG = Logger.getLogger( ImageServerConnectionImpl.class );

	private final File tmpDir = new File( "/temp/images/" );

	private final String repositoryUrl;
	private final String managementUrl;
	private final int applicationId;
	private final int groupId;
	private final String authKey;

    /**
    * Constructs an <code>ImageServerConnectionImpl</code> object.
     *
     * <p> The returned object as is can be only used to retrieve urls of images.</p>
     *
     * @param repositoryUrl the absolute url to the repository on the image server
     * @param applicationId a unique  id that is used to identify an application on the image server
     * @param groupId a unique id that is used to identify a group on the image server
    */
	public ImageServerConnectionImpl( String repositoryUrl, int applicationId, int groupId )
	{
		this( repositoryUrl, applicationId, groupId, null, null);
	}

    /**
    * Constructs an <code>ImageServerConnectionImpl</code> object with authentication information.
     *
     * <p>This type of constructor with authentication information is used when images are to be uploaded and modified.</p>
     *
     * @param repositoryUrl the absolute url to the repository on the image server
     * @param applicationId an unique  id that is used to identify an application on the image server
     * @param groupId an unique id that is used to identify a group on the image server
     * @param managementUrl the absolute url to the management page of the image server
     * @param authKey an unique authentication key that is used for server side authentication
    */
	public ImageServerConnectionImpl( String repositoryUrl, int applicationId, int groupId,
	                                  String managementUrl, String authKey)
	{
		this.repositoryUrl = repositoryUrl;
		this.applicationId = applicationId;
		this.groupId = groupId;
		this.managementUrl = managementUrl;
		this.authKey = authKey;
	}

    /**
     *  The image server can be configured to only stream images back but not allow uploading, replacing or cropping of
     *  images. Hence this method
     *  checks whether images can only be retrieved. This is especially the case when no authentication key is specified
     *  when an <code>ImageServerConnectionImpl</code> object is constructed.
     *
     * @return true if images can only be retrieved
     */
	public final boolean isReadOnly()
	{
		return ( authKey == null );
	}

    /**
     * Uploads an image to the image server.
     *
     * Returns an {@link ImageServerUploadResult} result that can be used to determine whether the image has been uploaded
     * successfully.
     *
     * @param image an {@link ImageModel} object that contains the data and information of the image to be uploaded
     * @return an {@link ImageServerUploadResult} object
     */
	public final ImageServerUploadResult uploadImage( ImageModel image )
	{
		return transferImage( null, image );
	}

    /**
     * Replaces an existing image on the image server
     *
     * Returns an {@link ImageServerUploadResult} result that can be used to determine whether the image has been replaced
     * successfully.
     *
     * @param imageId the image id as is given back in the {@link ImageServerUploadResult} object when uploading an image
     * @param image an {@link ImageModel} object that contains the data and information of the image to be replaced
     * @return an {@link ImageServerUploadResult} object
     */
	public final ImageServerUploadResult replaceImage( String imageId, ImageModel image )
	{
		return transferImage( imageId, image );
	}

	private ImageServerUploadResult transferImage( String id, ImageModel image )
	{
		verifyWritePermissions();

		try {
			File f = File.createTempFile( "upload", ".img", tmpDir );
			FileUtils.writeByteArrayToFile( f, image.getImageData().getBytes() );

			FileBody bin = new FileBody( f );
			StringBody authKeyBody = new StringBody( authKey );
			StringBody originalFilename = new StringBody( image.getImageData().getOriginalFilename() );

			StringBody imageKey = new StringBody( ( id == null )? "" : id );

			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart( "image", bin );
			reqEntity.addPart( "originalFilename", originalFilename  );
			reqEntity.addPart( "userKey", authKeyBody );
			reqEntity.addPart( "imageKey", imageKey );

			HttpResponse response = request( url( Action.UPLOAD ), reqEntity );

			String responsebody = httpResponseBody( response );

			LOG.info( responsebody );

			ImageServerUploadResult result = parseResponse( responsebody );

			return result;
		}
		catch ( IOException e ) {
			LOG.error( "Exception has occured.", e );
			return new ImageServerUploadResult( UploadStatus.CLIENT_ERROR, null );
		}
	}

	public final UploadStatus deleteImage( String id )
	{
		verifyWritePermissions();

		try {
			List<NameValuePair> parameters = new ArrayList<NameValuePair>();

			parameters.add( new BasicNameValuePair( "userKey", authKey ) );
			parameters.add( new BasicNameValuePair( "imageKey", id ) );

			UrlEncodedFormEntity entity = new UrlEncodedFormEntity( parameters );

			HttpResponse response = request( url( Action.DELETE ), entity );

			String responsebody = httpResponseBody( response );

			LOG.info( responsebody );

			try {
				return UploadStatus.getById( Integer.parseInt( responsebody, 10 ) );
			} catch ( NumberFormatException nf ) {
				LOG.error( "not a valid reponse code:" + responsebody );
				return UploadStatus.PARSE_ERROR;
			}
		}
		catch ( IOException e ) {
			LOG.error( "Exception has occured.", e );
			return UploadStatus.CLIENT_ERROR;
		}

	}

	private void verifyWritePermissions()
	{
		if( isReadOnly() ) {
			throw new AccessControlException( "connection is read-only" );
		}
	}

	private String url( Action action )
	{
		return new StringBuilder()
		.append( managementUrl )
		.append( "/")
		.append( Integer.toString( applicationId ) )
		.append( "/" )
		.append( Integer.toString( groupId ) )
		.append( "/" )
		.append( action.getUrlCode() )
		.toString();
	}


	private String httpResponseBody( HttpResponse response ) throws IOException
	{
		InputStream is = null;

		try {
			StringBuffer sb = new StringBuffer();
			is = response.getEntity().getContent();

			byte buffer[] = new byte[8*1024];
			int num;

			while ((num = is.read(buffer))!=-1) {
				sb.append(new String( buffer, 0, num ));
			}

			return sb.toString();

		} finally {
			if( is != null) {
				try {
					is.close();
				} catch ( IOException e ) {

				}
			}
		}
	}

	private ImageServerUploadResult parseResponse( String response )
	{
		UploadStatus status = UploadStatus.PARSE_ERROR;
		String imageId = null;

		String idMarker = ",id=";
		int ix = response.indexOf( idMarker );

		if( ix != -1 ) {
			try {

				int statusId = Integer.parseInt( response.substring( 0, ix ), 10 );
				status = UploadStatus.getById( statusId );
				if( ! status.isFailure() ) {
					imageId = response.substring( ix + idMarker.length() );
				}

			} catch ( NumberFormatException ne ) {
				LOG.error( "failed to parse " + response);
			}
		}

		return new ImageServerUploadResult( status, imageId );
	}

    /**
     * <p>Returns a String that represents the absolute url of the requested image on the image server.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @return The absolute url of the requested image on the image server for example: http://imageserverdomain.com/2164/12/14/541_400x600_0.jpg
     *
     *  <p>An empty string ("") will be returned if no valid url can be made from the given information. </p>
     */
	public final String getImageUrl( String imageId )
	{
		return getImageUrl( imageId, 0, 0, 0, null);
	}

    /**
     * <p>Returns a String that represents the absolute url of the requested image on the image server.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @param width the width of the image in pixels
     * @return The absolute url of the requested image on the image server for example: http://imageserverdomain.com/2164/12/14/541_400x600_0.jpg
     *
     *  <p>An empty string ("") will be returned if no valid url can be made from the given information. </p>
     */
	public final String getImageUrl( String imageId, int width)
	{
		return getImageUrl( imageId, width, 0, 0, null);
	}

    /**
     * <p>Returns a String that represents the absolute url of the requested image on the image server.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @return The absolute url of the requested image on the image server for example: http://imageserverdomain.com/2164/12/14/541_400x600_0.jpg
     *
     *  <p>An empty string ("") will be returned if no valid url can be made from the given information. </p>
     */
	public final String getImageUrl( String imageId, int width, int height)
	{
		return getImageUrl( imageId, width, height, 0, null);
	}

    /**
     * <p>Returns a String that represents the absolute url of the requested image on the image server.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param version the version of the image, version can only be 0 or above, relevant when dealing with different versions for one crop
     * @return The absolute url of the requested image on the image server for example: http://imageserverdomain.com/2164/12/14/541_400x600_0.jpg
     *
     *  <p>An empty string ("") will be returned if no valid url can be made from the given information. </p>
     */
	public final String getImageUrl( String imageId, int width, int height, int version )
	{
		return getImageUrl( imageId, width, height, version, null);
	}


    /**
     * <p>Returns a String that represents the absolute url of the requested image on the image server.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @param width the width of the image in pixels
     * @param height the height of the image in pixels
     * @param version the version of the image, version can only be 0 or above, relevant when dealing with different versions for one crop
     * @param fileType this represents the extension of the image
     * @return The absolute url of the requested image on the image server for example: http://imageserverdomain.com/2164/12/14/541_400x600_0.jpg
     *
     *  <p>An empty string ("") will be returned if no valid url can be made from the given information. </p>
     */
	public final String getImageUrl( String imageId, int width, int height, int version, String fileType )
	{
		int ix = imageId.lastIndexOf( '.' );
		String basePath = imageId.substring( 0, ix );
		String defaultExtension = imageId.substring( ix+1 );

		StringBuilder path = new StringBuilder();
			path.append( repositoryUrl );
			path.append( basePath );

		if( (width != 0) || (height != 0) ) {
		    path.append( "_" );
		    if( width != 0 ) {
		        path.append( width );
		    }
		    path.append( "x" );
		    if( height != 0 ) {
		        path.append( height );
		    }
		    if( version != 0 ) {
		        path.append( "_" );
		        path.append( version );
		    }
		}
		path.append( "." );

		path.append( ( fileType == null )? defaultExtension : fileType );

		return path.toString();
	}

    /**
     *  <p>Returns a String that represents the absolute url for the crop editing page of the specified image.</p>
     *
     * @param imageId the image id as is given back in the {@link com.foreach.imageserver.api.models.ImageServerUploadResult} object when uploading an image
     * @return The absolute url crop editing page for this image.
     */
    public final String getImageCropUrl( String imageId )
	{
		return getImageCropUrl( imageId, 0 );
	}

	public final String getImageCropUrl(String imageId, int version)
	{
		StringBuilder path = new StringBuilder();
		path.append( managementUrl );
		path.append( "/crop/" );
		path.append( encodedImageId( imageId ) );
		if( version != 0) {
			path.append( "/version/" );
			path.append( Integer.toString( version, 10 ) );
		}

        return path.toString();
	}

	private String encodedImageId( String imageId)
	{
		Base64 base64 = new Base64( true );
		int ix = imageId.lastIndexOf( '.' );
		String basePath = imageId.substring( 0, ix );
		return base64.encodeToString(basePath.getBytes()).replace("\n","").replace("\r","");
	}

	private static enum Action
	{
		UPLOAD ( "upload" ),
		REPLACE( "upload" ),
		DELETE ( "delete" );

		private String urlCode;

		Action( String urlCode )
		{
			this.urlCode = urlCode;
		}

		public String getUrlCode()
		{
			return urlCode;
		}
	}
}
