package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Crop;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.dto.CropDto;
import com.foreach.imageserver.dto.DimensionsDto;
import com.foreach.imageserver.dto.ImageModificationDto;
import com.foreach.imageserver.dto.ImageResolutionDto;
import org.junit.Test;

import static com.foreach.imageserver.core.services.CropGeneratorUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CropGeneratorUtilTest {

    @Test
    public void normalizeModification() {
        ImageModificationDto mod = new ImageModificationDto();
        CropDto crop = new CropDto(0, 0, 450, 300);
        crop.setBox(new DimensionsDto(450, 300));

        mod.setCrop(crop);
        mod.setResolution(new ImageResolutionDto(300, 200));

        Image original = new Image();
        original.setDimensions(new Dimensions(800, 600));

        CropGeneratorUtil.normalizeModificationDto(original, mod);

        assertEquals(new CropDto(0, 0, 800, 600), mod.getCrop());
    }

    @Test
    public void applyExactResolution() {
        Dimensions result = applyResolution(image(1000, 2000), resolution(3000, 4000));
        assertEquals(3000, result.getWidth());
        assertEquals(4000, result.getHeight());
    }

    @Test
    public void applyUnboundedWidthResolution() {
        Dimensions result = applyResolution(image(1000, 2000), resolution(0, 4000));
        assertEquals(2000, result.getWidth());
        assertEquals(4000, result.getHeight());
    }

    @Test
    public void applyUnboundedHeightResolution() {
        Dimensions result = applyResolution(image(1000, 2000), resolution(3000, 0));
        assertEquals(3000, result.getWidth());
        assertEquals(6000, result.getHeight());
    }

    @Test
    public void calculateArea() {
        Crop crop = new Crop(1234, 4321, 254, 782);
        assertEquals(198628, area(crop));
    }

    @Test
    public void noIntersection() {
        assertNull(intersect(new Crop(1000, 1000, 1000, 1000), new Crop(2001, 1000, 500, 500)));
        assertNull(intersect(new Crop(1000, 1000, 1000, 1000), new Crop(499, 1000, 500, 500)));
        assertNull(intersect(new Crop(1000, 1000, 1000, 1000), new Crop(1000, 499, 500, 500)));
        assertNull(intersect(new Crop(1000, 1000, 1000, 1000), new Crop(1000, 2001, 500, 500)));
    }

    @Test
    public void intersection() {
        Crop intersection1 = intersect(new Crop(500, 500, 1000, 1000), new Crop(1000, 1000, 1000, 1000));
        assertEquals(new Crop(1000, 1000, 500, 500), intersection1);

        Crop intersection2 = intersect(new Crop(1000, 1000, 1000, 1000), new Crop(500, 500, 1000, 1000));
        assertEquals(new Crop(1000, 1000, 500, 500), intersection2);

        Crop intersection3 = intersect(new Crop(500, 500, 2000, 2000), new Crop(600, 800, 1000, 900));
        assertEquals(new Crop(600, 800, 1000, 900), intersection3);
    }

    private Image image(int w, int h) {
        Image image = new Image();
        image.setDimensions(new Dimensions(w, h));
        return image;
    }

    private ImageResolution resolution(int w, int h) {
        ImageResolution resolution = new ImageResolution();
        resolution.setWidth(w);
        resolution.setHeight(h);
        return resolution;
    }

}
