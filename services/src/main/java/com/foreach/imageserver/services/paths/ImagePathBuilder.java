package com.foreach.imageserver.services.paths;

import com.foreach.imageserver.business.image.ServableImageData;

import java.util.Date;

public interface ImagePathBuilder {

    String createManualImagePath(
            ImageType imageType,
            int applicationId,
            int groupId,
            String pathYear,
            String pathMonth,
            String pathDay,
            ImageSpecifier imageSpecifier);


    String createManualImagePath(
            ImageType imageType,
            int applicationId,
            int groupId,
            Date date,
            ImageSpecifier imageSpecifier);

    String createManualImageDirectory(
            ImageType imageType,
            int applicationId,
            int groupId,
            Date date );

    String generateOriginalImagePath( ServableImageData imageData );

    String generateVariantImagePath( ServableImageData imageData, ImageSpecifier imageSpecifier );

	String createChildPath( Date date );

	String createUrlPath( ServableImageData imageData );

	String createUrlPath( ServableImageData imageData, ImageSpecifier imageSpecifier );

	String createRemoteId( ServableImageData imageData );

	long imageIdFromRemoteId( String remoteId );
}
