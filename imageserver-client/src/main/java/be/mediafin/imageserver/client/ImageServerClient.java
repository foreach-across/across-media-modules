package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.*;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

public interface ImageServerClient {

    String imageUrl(String imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType);

    String imageUrl(String imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant);

    InputStream imageStream(String imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType);

    InputStream imageStream(String imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant);

    DimensionsDto loadImage(String imageId, int dioContentId);

    DimensionsDto loadImage(String imageId, byte[] imageBytes);

    DimensionsDto loadImage(String imageId, byte[] imageBytes, Date imageDate);

    boolean imageExists(String imageId);

    ImageInfoDto imageInfo(String imageId);

    void registerImageModification(String imageId, ImageServerContext context, ImageModificationDto imageModificationDto);

    void registerImageModification(String imageId, ImageServerContext context, Integer width, Integer height, int cropX, int cropY, int cropWidth, int croptHeight, int densityWidth, int densityHeight);

    List<ImageResolutionDto> listAllowedResolutions(ImageServerContext context);

    List<ImageResolutionDto> listConfigurableResolutions(ImageServerContext context);

    List<ImageModificationDto> listModifications(String imageId, ImageServerContext context);

}
