package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.test.MockedLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the reference set of examples of image modifier normalization,
 * as used in the documentation on http://confluence.projects.foreach.be/display/IS/Image+modifier+parameters.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TestImageService.TestConfig.class}, loader = MockedLoader.class)
public class TestImageModifierExamples {
    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageStoreService imageStoreService;

    private final Image original = originalWithKnownDimensionsAndImageType();

    @Test
    public void otherOutputType() {
        verify(request(ImageType.PNG), response(ImageType.PNG));
        verify(request(ImageType.GIF), response(ImageType.GIF));
    }

    @Test
    public void imageSmallerThanOriginal() {
        verify(request(500, 300), response(500, 300));
    }

    @Test
    public void imageLargerThanOriginal() {
        verify(request(700, 500), response(587, 419));
        verify(request(700, 500, true), response(700, 500, true));
    }

    @Test
    public void imageWithSpecificDimensionAndOtherAccordingToOriginalAspect() {
        verify(request(700, 0), response(628, 419));
        verify(request(0, 300), response(450, 300));
        verify(request(700, 0, true), response(700, 467, true));
    }

    @Test
    public void largestImageFittingInFrame() {
        verify(request(700, 700, false, true), response(628, 419, false, true));
        verify(request(500, 300, false, true), response(500, 334, false, true));
//		verify( request( 1000, 500, true, true ), response( 749, 500, true, true ) );
    }

    private ImageVariant request(ImageType output) {
        return modifier(0, 0, false, false, output);
    }

    private ImageVariant request(int width, int height) {
        return request(width, height, false);
    }

    private ImageVariant request(int width, int height, boolean stretch) {
        return request(width, height, stretch, false);
    }

    private ImageVariant request(int width, int height, boolean stretch, boolean keepAspect) {
        return modifier(width, height, stretch, keepAspect, null);
    }

    private ImageVariant response(ImageType output) {
        return modifier(original.getDimensions().getWidth(), original.getDimensions().getHeight(), false, false,
                output);
    }

    private ImageVariant response(int width, int height) {
        return response(width, height, false);
    }

    private ImageVariant response(int width, int height, boolean stretch) {
        return response(width, height, stretch, false);
    }

    private ImageVariant response(int width, int height, boolean stretch, boolean keepAspect) {
        return modifier(width, height, stretch, keepAspect, original.getImageType());
    }

    private ImageVariant modifier(int width, int height, boolean stretch, boolean keepAspect, ImageType output) {
        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(width);
        modifier.getModifier().setHeight(height);
        modifier.getModifier().setOutput(output);
        modifier.getModifier().setStretch(stretch);
        modifier.getModifier().setKeepAspect(keepAspect);

        return modifier;
    }

    private void verify(ImageVariant request, final ImageVariant expectedModifier) {
        final ImageFile expectedImageFile = mock(ImageFile.class);

        when(imageStoreService.getImageFile(eq(original), any(ImageVariant.class))).thenAnswer(
                new Answer<ImageFile>() {
                    @Override
                    public ImageFile answer(InvocationOnMock invocation) throws Throwable {
                        ImageVariant normalized = (ImageVariant) invocation.getArguments()[1];

                        assertEquals(expectedModifier, normalized);

                        return expectedImageFile;
                    }
                });

        ImageFile imageFile = imageService.fetchImageFile(original, request);
        assertSame(expectedImageFile, imageFile);
    }

    private Image originalWithKnownDimensionsAndImageType() {
        Image image = new Image();
        image.setDimensions(new Dimensions(628, 419));
        image.setImageType(ImageType.JPEG);

        return image;
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageService imageService() {
            return new ImageServiceImpl();
        }
    }
}
