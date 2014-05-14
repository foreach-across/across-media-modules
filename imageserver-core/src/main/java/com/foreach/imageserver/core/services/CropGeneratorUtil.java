package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;

public class CropGeneratorUtil {

    public static Dimensions applyResolution(Image image, ImageResolution resolution) {
        return resolution.getDimensions().normalize(image.getDimensions());
        /*
        Integer resolutionWidth = resolution.getWidth();
        Integer resolutionHeight = resolution.getHeight();

        if (resolutionWidth != null && resolutionHeight != null) {
            return new Dimensions(resolutionWidth, resolutionHeight);
        } else {
            double originalWidth = image.getDimensions().getWidth();
            double originalHeight = image.getDimensions().getHeight();

            if (resolutionWidth != null) {
                return new Dimensions(resolutionWidth, (int) Math.round(resolutionWidth * (originalHeight / originalWidth)));
            } else {
                return new Dimensions((int) Math.round(resolutionHeight * (originalWidth / originalHeight)), resolutionHeight);
            }
        }*/
    }

    public static int area(Crop crop) {
        return crop.getWidth() * crop.getHeight();
    }

    public static Crop intersect(Crop crop1, Crop crop2) {
        int l1 = crop1.getX();
        int r1 = crop1.getX() + crop1.getWidth();
        int t1 = crop1.getY();
        int b1 = crop1.getY() + crop1.getHeight();

        int l2 = crop2.getX();
        int r2 = crop2.getX() + crop2.getWidth();
        int t2 = crop2.getY();
        int b2 = crop2.getY() + crop2.getHeight();

        if (l2 > r1 || r2 < l1 || t2 > b1 || b2 < t1) {
            // No intersection.
            return null;
        }

        int li = Math.max(l1, l2);
        int ri = Math.min(r1, r2);
        int ti = Math.max(t1, t2);
        int bi = Math.min(b1, b2);

        return new Crop(li, ti, ri - li, bi - ti);
    }

}
