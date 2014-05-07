package com.foreach.imageserver.core.transformers;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

import static com.foreach.imageserver.core.utils.ImageUtils.*;
import static org.junit.Assert.*;

public class ImageMagickImageTransformerTest extends AbstractIntegrationTest {

    @Autowired
    private ImageMagickImageTransformer imageTransformer;

    @Test
    public void canExecute() {
        for (ImageType imageType : ImageType.values()) {
            ImageTransformerPriority expectedPriority = (imageType == ImageType.EPS || imageType == ImageType.PDF) ? ImageTransformerPriority.UNABLE : ImageTransformerPriority.PREFERRED;
            assertEquals(expectedPriority, imageTransformer.canExecute(calculateDimensionsAction(imageType)));
            assertEquals(expectedPriority, imageTransformer.canExecute(modifyAction(imageType)));
            assertEquals(ImageTransformerPriority.PREFERRED, imageTransformer.canExecute(getImageAttributesAction()));
        }
    }

    @Test
    public void calculateDimensionsJpeg() {
        Dimensions dimensions = imageTransformer.execute(calculateDimensionsAction(ImageType.JPEG, "images/cropCorrectness.jpeg"));
        assertEquals(2000, dimensions.getWidth());
        assertEquals(1000, dimensions.getHeight());
    }

    @Test
    public void calculateDimensionsPng() {
        Dimensions dimensions = imageTransformer.execute(calculateDimensionsAction(ImageType.PNG, "images/cropCorrectness.png"));
        assertEquals(2000, dimensions.getWidth());
        assertEquals(1000, dimensions.getHeight());
    }

    @Test
    public void getImageAttributesJpeg() {
        ImageAttributes attributes = imageTransformer.execute(getImageAttributesAction("images/cropCorrectness.jpeg"));
        assertEquals(ImageType.JPEG, attributes.getType());
        assertEquals(2000, attributes.getDimensions().getWidth());
        assertEquals(1000, attributes.getDimensions().getHeight());
    }

    @Test
    public void getImageAttributesPng() {
        ImageAttributes attributes = imageTransformer.execute(getImageAttributesAction("images/cropCorrectness.png"));
        assertEquals(ImageType.PNG, attributes.getType());
        assertEquals(2000, attributes.getDimensions().getWidth());
        assertEquals(1000, attributes.getDimensions().getHeight());
    }

    @Test
    public void cropJpgToJpg() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.JPEG,
                "images/cropCorrectness.jpeg",
                270,
                580,
                1000,
                140,
                270,
                580,
                ImageType.JPEG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/cropJpgToJpg.jpeg")));
    }

    @Test
    public void cropPngToPng() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.PNG,
                "images/cropCorrectness.png",
                270,
                580,
                1000,
                140,
                270,
                580,
                ImageType.PNG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/cropPngToPng.png")));
    }

    @Test
    public void cropJpgToPng() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.JPEG,
                "images/cropCorrectness.jpeg",
                270,
                580,
                1000,
                140,
                270,
                580,
                ImageType.PNG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/cropJpgToPng.png")));
    }

    @Test
    public void cropPngToJpg() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.PNG,
                "images/cropCorrectness.png",
                270,
                580,
                1000,
                140,
                270,
                580,
                ImageType.JPEG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/cropPngToJpg.jpeg")));
    }

    @Test
    public void transparentPngToPng() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.PNG,
                "images/transparency.png",
                100,
                100,
                0,
                0,
                100,
                100,
                ImageType.PNG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/transparentPngToPng.png")));
    }

    @Test
    public void transparentPngToJpg() throws Exception {
        ImageModifyAction action = modifyAction(
                ImageType.PNG,
                "images/transparency.png",
                100,
                100,
                0,
                0,
                100,
                100,
                ImageType.JPEG);
        InMemoryImageSource result = imageTransformer.execute(action);
        assertNotNull(result);
        assertNotNull(result.getImageBytes());
        assertTrue(imagesAreEqual(bufferedImage(result.getImageBytes()), bufferedImageFromClassPath("images/transparentPngToJpg.jpg")));
    }

    @Test
    public void getOrder() throws Exception {
        assertEquals(3, imageTransformer.getOrder());
    }

    private ImageCalculateDimensionsAction calculateDimensionsAction(ImageType imageType) {
        return new ImageCalculateDimensionsAction(new StreamImageSource(imageType, (InputStream) null));
    }

    private GetImageAttributesAction getImageAttributesAction() {
        return new GetImageAttributesAction(null);
    }

    private ImageModifyAction modifyAction(ImageType sourceType, String classPath, int outputWidth, int outputHeight, int cropX, int cropY, int cropWidth, int cropHeight, ImageType outputType) {
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(classPath);
        return new ImageModifyAction(
                new StreamImageSource(sourceType, imageStream),
                outputWidth,
                outputHeight,
                cropX,
                cropY,
                cropWidth,
                cropHeight,
                0,
                0,
                outputType);
    }

    private ImageModifyAction modifyAction(ImageType imageType) {
        return new ImageModifyAction(new StreamImageSource(imageType, (InputStream) null), 0, 0, 0, 0, 0, 0, 0, 0, null);
    }

    private ImageCalculateDimensionsAction calculateDimensionsAction(ImageType imageType, String classPath) {
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(classPath);
        return new ImageCalculateDimensionsAction(new StreamImageSource(imageType, imageStream));
    }

    private GetImageAttributesAction getImageAttributesAction(String classPath) {
        InputStream imageStream = getClass().getClassLoader().getResourceAsStream(classPath);
        return new GetImageAttributesAction(imageStream);
    }
}
