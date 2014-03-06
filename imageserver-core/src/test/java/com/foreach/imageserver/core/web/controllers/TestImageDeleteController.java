package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.web.displayables.JsonResponse;
import com.foreach.test.MockedLoader;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Random;
import java.util.UUID;

import static org.mockito.Mockito.reset;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageDeleteController.TestConfig.class, loader = MockedLoader.class)
public class TestImageDeleteController {
    @Autowired
    private ImageDeleteController deleteController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageService imageService;

   @Test
    public void unknownApplicationReturnsPermissionDeniedForDelete() {
        JsonResponse response = deleteController.delete(1, UUID.randomUUID().toString(), "somekey");
        Assert.assertFalse(response.isSuccess());
        Mockito.verify(applicationService).getApplicationById(1);
    }

    @Test
    public void unknownApplicationReturnsPermissionDeniedForDeleteVariants() {
        JsonResponse response = deleteController.deleteVariants(201, UUID.randomUUID().toString(), "somekey");
        Assert.assertFalse(response.isSuccess());
        Mockito.verify(applicationService).getApplicationById(201);
    }

    @Test
    public void ifApplicationManagementNotAllowedThenPermissionDeniedForDelete() {

        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        String code = RandomStringUtils.random(10);
        Assert.assertFalse("Precondition on test data failed", application.canBeManaged(code));

        JsonResponse response = deleteController.delete(application.getId(), code, "somekey");

        Assert.assertFalse(response.isSuccess());
        Mockito.verify(applicationService).getApplicationById(application.getId());
    }

    @Test
    public void ifApplicationManagementNotAllowedThenPermissionDeniedForDeleteVariants() {

        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        String code = RandomStringUtils.random(10);
        Assert.assertFalse("Precondition on test data failed", application.canBeManaged(code));

        JsonResponse response = deleteController.deleteVariants(application.getId(), code, "somekey");

        Assert.assertFalse(response.isSuccess());
        Mockito.verify(applicationService).getApplicationById(application.getId());
    }

    @Test
    public void unknownImageGivesNoExceptionOnDelete() {
        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        JsonResponse response = deleteController.delete(application.getId(), application.getCode(), "someimagekey");

        Mockito.verify(imageService, Mockito.times(1)).getImageByKey("someimagekey", application.getId());
        Mockito.verify(imageService, Mockito.never()).deleteImageAndVariants(Matchers.any(Image.class));
        Assert.assertFalse(response.isSuccess());
    }

    @Test
    public void unknownImageGivesNoExceptionOnDeleteVariants() {
        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        deleteController.deleteVariants(application.getId(), application.getCode(), "someimagekey");

        Mockito.verify(imageService, Mockito.times(1)).getImageByKey("someimagekey", application.getId());
        Mockito.verify(imageService, Mockito.never()).deleteVariantsOfImage(Matchers.any(Image.class));
    }

    @Test
    public void validImageForDeleteWillResultInDeletion() {
        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        Image image = new Image();
        Mockito.when(imageService.getImageByKey("validimagekey", application.getId())).thenReturn(image);

        deleteController.delete(application.getId(), application.getCode(), "validimagekey");

        Mockito.verify(imageService, Mockito.times(1)).deleteImageAndVariants(image);
    }

    @Test
    public void validImageForDeleteVariantsWillResultInDeletion() {
        Application application = createApplication(true);
        Mockito.when(applicationService.getApplicationById(Matchers.anyInt())).thenReturn(application);

        Image image = new Image();
        Mockito.when(imageService.getImageByKey("validimagekey", application.getId())).thenReturn(image);

        deleteController.deleteVariants(application.getId(), application.getCode(), "validimagekey");

        Mockito.verify(imageService, Mockito.times(1)).deleteVariantsOfImage(image);
    }

    private Application createApplication(boolean active) {
        Application application = new Application();
        application.setId(new Random().nextInt());
        application.setCode(UUID.randomUUID().toString());
        application.setActive(active);

        return application;
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageDeleteController imageDeleteController() {
            return new ImageDeleteController();
        }
    }
}
