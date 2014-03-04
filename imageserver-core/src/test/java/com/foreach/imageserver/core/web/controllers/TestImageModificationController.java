package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.business.ImageVariant;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageVariantService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.exceptions.ImageModificationException;
import com.foreach.imageserver.core.web.dto.ImageModifierDto;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.web.exceptions.ImageNotFoundException;
import com.foreach.test.MockedLoader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageModificationController.TestConfig.class, loader = MockedLoader.class)
public class TestImageModificationController {
    @Autowired
    private ImageModificationController modificationController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageVariantService imageVariantService;

    @Test
    public void unknownApplicationReturnsPermissionDenied() {
        boolean exceptionWasThrown = false;

        try {
            modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());
        } catch (ApplicationDeniedException ade) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
        verify(applicationService).getApplicationById(1);
    }

    @Test
    public void ifApplicationManagementNotAllowedThenPermissionDenied() {
        boolean exceptionWasThrown = false;

        Application application = mock(Application.class);

        when(applicationService.getApplicationById(anyInt())).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(false);

        try {
            modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());
        } catch (ApplicationDeniedException ade) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
    }

    @Test(expected = ApplicationDeniedException.class)
    public void requestInactiveApplication() {
        Application inactive = new Application();
        inactive.setCode(UUID.randomUUID().toString());
        inactive.setActive(false);

        when(applicationService.getApplicationById(1)).thenReturn(inactive);

        modificationController.register(1, inactive.getCode(), "somekey", createModifierDto());
    }

    @Test(expected = ImageNotFoundException.class)
    public void requestUnknownImageForApplication() {
        Application application = mock(Application.class);

        when(applicationService.getApplicationById(1)).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(true);

        modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());
    }

    @Test(expected = ImageModificationException.class)
    public void requestEmptyTargetDimensions() {
        Application application = mock(Application.class);

        when(applicationService.getApplicationById(1)).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(true);

        modificationController.register(1, UUID.randomUUID().toString(), "somekey",
                createModifierDto(new Dimensions()));
    }

    @Test
    public void validDimensionsAndModifier() {
        Application application = mock(Application.class);
        when(application.getId()).thenReturn(1);

        when(applicationService.getApplicationById(1)).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(true);

        Image image = mock(Image.class);
        Dimensions dimensions = new Dimensions(800, 0);

        ImageModifierDto modifierDto = createModifierDto(dimensions);
        ImageVariant modifier = new ImageVariant(modifierDto);

        when(imageService.getImageByKey("somekey", 1)).thenReturn(image);

        modificationController.register(1, UUID.randomUUID().toString(), "somekey", modifierDto);

        verify(imageVariantService, times(1)).registerVariant(image, modifier);
    }

    private ImageModifierDto createModifierDto() {
        return createModifierDto(new Dimensions(800, 600));
    }

    private ImageModifierDto createModifierDto(Dimensions dimensions) {
        ImageModifierDto mod =
                new ImageModifierDto();
        mod.setHeight(dimensions.getHeight());
        mod.setWidth(dimensions.getWidth());
        return mod;
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageModificationController imageModificationController() {
            return new ImageModificationController();
        }
    }
}
