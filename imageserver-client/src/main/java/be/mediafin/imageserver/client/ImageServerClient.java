package be.mediafin.imageserver.client;

import com.foreach.imageserver.dto.*;

import java.io.InputStream;
import java.util.List;

public interface ImageServerClient {

    String imageUrl(int imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType);

    String imageUrl(int imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant);

    InputStream imageStream(int imageId, ImageServerContext context, Integer width, Integer height, ImageTypeDto imageType);

    InputStream imageStream(int imageId, ImageServerContext context, ImageResolutionDto imageResolution, ImageVariantDto imageVariant);

    DimensionsDto loadImage(int imageId, int dioContentId);

    void registerImageModification(int imageId, ImageServerContext context, ImageResolutionDto imageResolutionDto, ImageModificationDto imageModificationDto);

    void registerImageModification(int imageId, ImageServerContext context, Integer width, Integer height, int cropX, int cropY, int cropWidth, int croptHeight, int densityWidth, int densityHeight);

    List<ImageResolutionDto> listAllowedResolutions(ImageServerContext context);

    List<ModificationStatusDto> listModificationStatus(List<Integer> imageIds);

}
