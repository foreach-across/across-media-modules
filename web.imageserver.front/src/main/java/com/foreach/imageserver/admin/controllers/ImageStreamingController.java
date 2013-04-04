package com.foreach.imageserver.admin.controllers;

import com.foreach.imageserver.admin.rendering.ImageRenderingResult;
import com.foreach.imageserver.services.VariantImageLogger;
import com.foreach.imageserver.services.paths.ImageSpecifier;
import com.foreach.imageserver.services.paths.ImageType;
import com.foreach.imageserver.admin.service.ImageServerFacade;
import com.foreach.imageserver.admin.editors.ImageSpecifierEditor;
import com.foreach.imageserver.admin.rendering.ImageRenderingFacade;
import com.foreach.imageserver.services.paths.ImagePathBuilder;
import com.foreach.imageserver.business.geometry.Size;
import com.foreach.imageserver.business.image.ServableImageData;
import com.foreach.imageserver.business.image.VariantImage;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.services.FormatService;
import com.foreach.imageserver.services.VariantImageService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

/**
 * This is the main controller that will server up an image requested,
 * and create an actual cropped image if not found.
 */

@Controller
@RequestMapping("/repository")
public class ImageStreamingController
{
    private static final Logger LOG = Logger.getLogger(ImageUploadController.class);

	@Autowired
	private ImagePathBuilder pathBuilder;

	@Autowired
	private ImageServerFacade imageServerFacade;

	@Autowired
	private ImageRenderingFacade imageRenderingFacade;

    @Autowired
	private VariantImageService variantImageService;

    @Autowired
    private FormatService formatService;

    @Autowired
    private VariantImageLogger variantImageLogger;

    @InitBinder()
    protected final void initBinder( HttpServletRequest request, ServletRequestDataBinder binder )
    {
        binder.registerCustomEditor( ImageSpecifier.class, new ImageSpecifierEditor() );
    }

    @RequestMapping(
            "/{applicationId}/{groupId}/{pathYear}/{pathMonth}/{pathDay}/{imageSpecifier}")
    public final void streamVariantImage(
            @PathVariable int applicationId,
            @PathVariable int groupId,
            @PathVariable String pathYear,
            @PathVariable String pathMonth,
            @PathVariable String pathDay,
            @PathVariable ImageSpecifier imageSpecifier,
            HttpServletResponse response) throws IOException
    {
        streamVariantImage(applicationId, groupId, pathYear, pathMonth, pathDay, imageSpecifier, true, response);
    }

	@RequestMapping(
			"/{applicationId}/{groupId}/{pathYear}/{pathMonth}/{pathDay}/{imageSpecifier}/c{check}")
	public final void streamVariantImage(
			@PathVariable int applicationId,
			@PathVariable int groupId,
			@PathVariable String pathYear,
			@PathVariable String pathMonth,
			@PathVariable String pathDay,
			@PathVariable ImageSpecifier imageSpecifier,
            @PathVariable boolean check,
			HttpServletResponse response) throws IOException
	{
        try {
	        int width = imageSpecifier.getWidth();
			int height = imageSpecifier.getHeight();
			String fileExtension = imageSpecifier.getFileType();

			ImageType imageType = ( ( width == 0 ) && ( height == 0 ) ) ? ImageType.ORIGINAL : ImageType.VARIANT;

			String physicalPath =
					pathBuilder.createManualImagePath( imageType, applicationId, groupId, pathYear, pathMonth, pathDay,
													   imageSpecifier );

			File imageFile = new File( physicalPath );

			//Build a variant Image
			VariantImage variantImage = new VariantImage();
			variantImage.setImageId(imageSpecifier.getImageId());
			variantImage.setWidth(width);
			variantImage.setHeight(height);
			variantImage.setVersion(imageSpecifier.getVersion());

			if ( !imageFile.exists() ) {
				// If the original was not found, we can't do anything about it
				if ( imageType == ImageType.ORIGINAL ) {
					throw new NotFoundException();
				}

				ServableImageData imageData = imageServerFacade.getImageData( imageSpecifier.getImageId() );

				if ( imageData == null || imageData.isDeleted() ) {
					//no record with id found
					throw new NotFoundException();
				}

				String manualPathToOriginalFile =
						pathBuilder.createManualImagePath( ImageType.ORIGINAL, applicationId, groupId, pathYear, pathMonth,
														   pathDay, imageSpecifier );
				String generatedPathToOriginalFile = pathBuilder.generateOriginalImagePath( imageData );

				if ( !StringUtils.equals( generatedPathToOriginalFile, manualPathToOriginalFile ) ) {
					// parameters don't match with the image record
					throw new NotFoundException();
				}

				Group group = imageServerFacade.getImageGroup( groupId );

				Size size = new Size( width, height );

				// Is the requested format allowed on the group?
				if ( check && !group.isFormatAllowed( size ) ) {
					throw new NotFoundException();
				}

				// Generate the new variant of the image
				ImageRenderingResult renderResult = imageRenderingFacade.generateVariant( imageData, imageSpecifier );
				File generatedFile = renderResult.getTargetFile();

				// Double check that the generatedFile has the expected path
				if ( !StringUtils.equals( generatedFile.getAbsolutePath(), imageFile.getAbsolutePath() ) ) {
					throw new NotFoundException();
				}

				//store the variant in the DB
				int formatId = formatService.getFormatIdForDimension(size, groupId);
				LOG.error( "format id=" + formatId + " for size "+size );
				variantImage.setFormatId( formatId );
				if ( renderResult.getCropId() != null){
					variantImage.setCropId( renderResult.getCropId() );
				}
				variantImage.setLastCalled( new Date() );

				try{
					variantImageService.saveVariantImage(variantImage);
				}
				catch( DataIntegrityViolationException e){
					//is usually reached when the variant is physically removed from local disk
					LOG.error("Exception has occured.", e);
					// Keep FileSystem and Database synchronized
					renderResult.removeTargetFile();
				}
			}

			if (imageType == ImageType.VARIANT) {
				// Log access
				variantImageLogger.logVariantImage(variantImage);
			}

			streamImageFile( imageFile, fileExtension, response );
        } catch (Exception e ) {
	        LOG.error( "failed to stream " + imageSpecifier, e );
        }
	}

	private void streamImageFile( File imageFile, String extension, HttpServletResponse response ) throws IOException
	{
		response.setContentType( "image/" + extension );
		response.setContentLength( Long.valueOf( imageFile.length() ).intValue() );

		FileInputStream in = null;
		OutputStream out = null;

		try {
		    // Open the file and output streams within the try catch block

            in = new FileInputStream( imageFile );
            out = response.getOutputStream();

			// Copy the contents of the file to the output stream
			byte[] buf = new byte[1024];

			int count = 0;
			while ( ( count = in.read( buf ) ) >= 0 ) {
				out.write( buf, 0, count );
			}

		}
		finally {
			if ( in != null) {
                try { in.close();
                } catch (Exception e) {

                }
            }
            if ( out != null) {
                try {
                    out.close();
                } catch (Exception e) {

                }
            }
        }
	}

	@ResponseStatus(HttpStatus.NOT_FOUND)
	public static class NotFoundException extends RuntimeException
	{
	}
}
