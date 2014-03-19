package be.mediafin.imageserver.imagerepositories.diocontent.business;

import be.mediafin.imageserver.imagerepositories.diocontent.DioContentImageRepository;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageParameters;
import com.foreach.imageserver.core.business.ImageType;

public class DioContentImageParameters implements ImageParameters {
    private int imageId;
    private int dioContentId;
    private Dimensions dimensions;
    private ImageType imageType;

    @Override
    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public int getDioContentId() {
        return dioContentId;
    }

    public void setDioContentId(int dioContentId) {
        this.dioContentId = dioContentId;
    }

    @Override
    public String getRepositoryCode() {
        return DioContentImageRepository.CODE;
    }

    @Override
    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    @Override
    public Dimensions getDimensions() {
        return dimensions;
    }

    public void setDimensions(Dimensions dimensions) {
        this.dimensions = dimensions;
    }

    @Override
    public String getUniqueFileName() {
        return getImageId() + "-dc" + getDioContentId() + "." + getImageType().getExtension();
    }
}
