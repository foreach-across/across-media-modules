package com.foreach.imageserver.admin.models;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.image.Format;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.business.image.ServableImageData;

import java.util.List;

public class ImageDetailsModel
{
    private ServableImageData image;
    private Application application;
    private Group group;
    private List<Format> formats;
    private String imagePath;
    private String originalPath;

    public final ServableImageData getImage() {
        return image;
    }

    public final void setImage(ServableImageData image) {
        this.image = image;
    }

    public final Application getApplication() {
        return application;
    }

    public final void setApplication(Application application) {
        this.application = application;
    }

    public final Group getGroup() {
        return group;
    }

    public final void setGroup(Group group) {
        this.group = group;
    }

    public final String getImagePath() {
        return imagePath;
    }

    public final void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public final List<Format> getFormats() {
        return formats;
    }

    public final void setFormats(List<Format> formats) {
        this.formats = formats;
    }

    public final int getCropListSize()
    {
        return this.image.getCrops().size();
    }

    public final String getOriginalPath() {
        return originalPath;
    }

    public final void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
}
