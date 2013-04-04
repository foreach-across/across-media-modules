package com.foreach.imageserver.admin.viewHelpers;

import com.foreach.imageserver.business.image.ServableImageData;

import java.util.List;

public class ImagesViewHelper {
    private List<ServableImageData> list;

    public ImagesViewHelper(List<ServableImageData> images) {
        this.list = images;
    }

    public final List<ServableImageData> getList() {
        return list;
    }

    public final void setList(List<ServableImageData> list) {
        this.list = list;
    }

    public final int getNumberOfImages() {
        return list.size();
    }

}
