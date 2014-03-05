package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageModificationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.ImageVariantService;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.imageserver.core.web.dto.ImageModificationDto;
import com.foreach.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestImageModificationController.TestConfig.class, loader = MockedLoader.class)
public class TestImageModificationController {
    @Autowired
    private ImageModificationController modificationController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageModificationService imageModificationService;

    @Autowired
    private ImageVariantService imageVariantService;

    @Before
    public void setup() {
        reset(applicationService, imageService, imageModificationService, imageVariantService);
    }

    @Test
    public void unknownApplicationReturnsPermissionDenied() {

        JsonResponse response = modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());
        assertFalse(response.isSuccess());
        verify(applicationService).getApplicationById(1);
    }

    @Test
    public void ifApplicationManagementNotAllowedThenPermissionDenied() {
        Application application = mock(Application.class);

        when(applicationService.getApplicationById(anyInt())).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(false);

        JsonResponse response = modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());

        assertFalse(response.isSuccess());
    }

    public void requestInactiveApplication() {
        Application inactive = new Application();
        inactive.setCode(UUID.randomUUID().toString());
        inactive.setActive(false);

        when(applicationService.getApplicationById(1)).thenReturn(inactive);

        JsonResponse response = modificationController.register(1, inactive.getCode(), "somekey", createModifierDto());
        assertFalse(response.isSuccess());
    }

    public void requestUnknownImageForApplication() {
        Application application = mock(Application.class);

        when(applicationService.getApplicationById(1)).thenReturn(application);
        when(application.canBeManaged(anyString())).thenReturn(true);

        JsonResponse response = modificationController.register(1, UUID.randomUUID().toString(), "somekey", createModifierDto());
        assertFalse(response.isSuccess());
    }

    @Test
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

        ImageModificationDto modifierDto = createModifierDto(dimensions);
        ImageModification modifier = new ImageModification();
        modifier.setCrop(modifierDto.getCrop());
        modifier.getVariant().setHeight(modifierDto.getHeight());
        modifier.getVariant().setWidth(modifierDto.getWidth());

        when(imageService.getImageByKey("somekey", 1)).thenReturn(image);

        ImageVariant existingVariant = new ImageVariant();
        existingVariant.setWidth(800);
        when(imageVariantService.getExactVariantForModification(eq(application), eq(modifierDto))).thenReturn(existingVariant);

        modificationController.register(1, UUID.randomUUID().toString(), "somekey", modifierDto);

        verify(imageModificationService, times(1)).saveModification(image, modifier);
    }

    private ImageModificationDto createModifierDto() {
        return createModifierDto(new Dimensions(800, 600));
    }

    private ImageModificationDto createModifierDto(Dimensions dimensions) {
        ImageModificationDto mod =
                new ImageModificationDto();
        mod.setHeight(dimensions.getHeight());
        mod.setWidth(dimensions.getWidth());
        mod.getCrop().setHeight(100);
        mod.getCrop().setWidth(100);
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
