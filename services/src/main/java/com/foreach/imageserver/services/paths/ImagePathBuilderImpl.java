package com.foreach.imageserver.services.paths;

import com.foreach.imageserver.business.image.ServableImageData;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ImagePathBuilderImpl implements ImagePathBuilder
{
	private String rootPath = "/temp";

    public final void setRootpath( String rootPath )
    {
        this.rootPath = rootPath;
    }

	public final String createManualImagePath(
			ImageVersion imageVersion,
			int applicationId,
			int groupId,
			String pathYear,
			String pathMonth,
			String pathDay,
            ImageSpecifier imageSpecifier)
	{
		StringBuilder path = new StringBuilder( rootPath )
                .append( "/" )
                .append( applicationId )
                .append( "/" )
                .append( groupId )
                .append( "/" )
                .append( imageVersion.getPath() )
                .append( "/" )
                .append( pathYear )
                .append( "/" )
                .append( pathMonth )
                .append( "/" )
                .append( pathDay )
                .append( "/" );

		if ( imageVersion == ImageVersion.VARIANT ) {
			path.append( imageSpecifier.toString() );
		} else {
            path.append( imageSpecifier.baseImageName() );
        }

		return path.toString();
	}

    public final String createManualImagePath(
			ImageVersion imageVersion,
			int applicationId,
			int groupId,
			Date date,
			ImageSpecifier imageSpecifier)
	{
        FastDateFormat yearFormat = FastDateFormat.getInstance( "yyyy", TimeZone.getDefault(), Locale.getDefault() );
        FastDateFormat monthFormat = FastDateFormat.getInstance( "MM", TimeZone.getDefault(), Locale.getDefault() );
        FastDateFormat dayFormat = FastDateFormat.getInstance( "dd", TimeZone.getDefault(), Locale.getDefault() );

        String pathYear =  yearFormat.format(date);
        String pathMonth =  monthFormat.format(date);
        String pathDay =  dayFormat.format(date);

        return createManualImagePath( imageVersion, applicationId, groupId, pathYear, pathMonth, pathDay, imageSpecifier);
	}

    public final String createManualImageDirectory(
			ImageVersion imageVersion,
			int applicationId,
			int groupId,
			Date date )
	{
        FastDateFormat dateFormat = FastDateFormat.getInstance( "yyyy/MM/dd", TimeZone.getDefault(), Locale.getDefault() );

        String pathDate =  dateFormat.format(date);

		StringBuilder path = new StringBuilder( rootPath )
                .append( "/" )
                .append( applicationId )
                .append( "/" )
                .append( groupId )
                .append( "/" )
                .append( imageVersion.getPath() )
                .append( "/" )
                .append( pathDate );

		return path.toString();
	}

	public final String generateOriginalImagePath( ServableImageData imageData )
	{
		ImageSpecifier imageSpecifier = new ImageSpecifier();
        imageSpecifier.setImageId( imageData.getId() );
        imageSpecifier.setFileType( imageData.getExtension() );

        return generateImagePath( ImageVersion.ORIGINAL, imageData, imageSpecifier );
	}

	public final String generateVariantImagePath( ServableImageData imageData, ImageSpecifier imageSpecifier )
	{
		return generateImagePath( ImageVersion.VARIANT, imageData, imageSpecifier );
	}

	private String generateImagePath(
			ImageVersion imageVersion,
			ServableImageData imageData,
			ImageSpecifier imageSpecifier)
	{
		StringBuilder path =
				new StringBuilder( rootPath )
                        .append( "/" )
                        .append( imageData.getApplicationId() )
                        .append( "/" )
                        .append( imageData.getGroupId() )
                        .append( "/" )
                        .append( imageVersion.getPath() )
                        .append( "/" )
                        .append( imageData.getPath() )
                        .append( "/" );

        if ( imageVersion == ImageVersion.VARIANT ) {
            path.append( imageSpecifier.toString() );
        } else {
            path.append( imageSpecifier.baseImageName() );
        }

		return path.toString();
	}

	public final String createChildPath( Date date )
	{
		FastDateFormat dateFormat = FastDateFormat.getInstance( "yyyy/MM/dd", TimeZone.getDefault(), Locale.getDefault() );

		return dateFormat.format( date );
	}

	public final String createUrlPath( ServableImageData imageData )
	{
        ImageSpecifier imageSpecifier = new ImageSpecifier();
        imageSpecifier.setImageId( imageData.getId() );
        imageSpecifier.setFileType( imageData.getExtension() );

		return createUrlPath( imageData,  imageSpecifier );
	}

	public final String createUrlPath( ServableImageData imageData, ImageSpecifier imageSpecifier )
	{
        imageSpecifier.setImageId( imageData.getId() );
        imageSpecifier.setFileType( imageData.getExtension() );

		return new StringBuilder()
						.append( "repository/" )
                        .append( imageData.getApplicationId() )
                        .append( "/" )
                        .append( imageData.getGroupId() )
                        .append( "/" )
                        .append( imageData.getPath() )
                        .append( "/" )
                        .append( imageSpecifier.toString() )
                        .toString();
	}

	public final String createRemoteId( ServableImageData imageData )
	{
		return new StringBuilder()
						.append( "/" )
                        .append( imageData.getApplicationId() )
                        .append( "/" )
                        .append( imageData.getGroupId() )
                        .append( "/" )
                        .append( imageData.getPath() )
                        .append( "/" )
                        .append( imageData.getId() )
						.append( "." )
						.append( imageData.getExtension() )
                        .toString();
	}

	public final long imageIdFromRemoteId( String remoteId )
	{
		int ix = remoteId.lastIndexOf( '/' );
		int iy = remoteId.lastIndexOf( '.' );
		try {
			return Long.parseLong( remoteId.substring( ix+1, iy ), 10 );
		} catch ( NumberFormatException ne ) {
			return 0L;
		}
	}
}
