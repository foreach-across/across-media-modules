package com.foreach.imageserver.core.web.controllers;

import com.foreach.imageserver.core.business.Application;
import com.foreach.imageserver.core.business.Image;
import com.foreach.imageserver.core.services.ApplicationService;
import com.foreach.imageserver.core.services.ImageService;
import com.foreach.imageserver.core.services.repositories.ImageLookupRepository;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupStatus;
import com.foreach.imageserver.core.web.exceptions.ApplicationDeniedException;
import com.foreach.imageserver.core.web.exceptions.ImageForbiddenException;
import com.foreach.imageserver.core.web.exceptions.ImageLookupException;
import com.foreach.imageserver.core.web.exceptions.ImageNotFoundException;
import com.foreach.test.MockedLoader;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
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

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = TestImageLoadController.TestConfig.class, loader = MockedLoader.class)
public class TestImageLoadController {
    private final Random RANDOM = new Random(System.currentTimeMillis());

    @Autowired
    private ImageLoadController loadController;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ImageLookupRepository repositoryOne;

    @Autowired
    private ImageLookupRepository repositoryTwo;

    @Autowired
    private ImageService imageService;

    @Before
    public void before() {
        when(repositoryOne.getCode()).thenReturn("web");
    }

    @Test
    public void unknownApplicationReturnsPermissionDenied() {
        boolean exceptionWasThrown = false;

        try {
            loadController.load(1, UUID.randomUUID().toString(), "web", null, map("web.url", "http://someimageurl"));
        } catch (ApplicationDeniedException ade) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
        verify(applicationService).getApplicationById(1);
    }

    @Test
    public void ifApplicationManagementNotAllowedThenPermissionDenied() {
        boolean exceptionWasThrown = false;

        Application application = createApplication(true);
        when(applicationService.getApplicationById(anyInt())).thenReturn(application);

        String code = RandomStringUtils.random(10);
        assertFalse("Precondition on test data failed", application.canBeManaged(code));

        try {
            loadController.load(application.getId(), code, "web", null, map("web.url", "http://someimageurl"));
        } catch (ApplicationDeniedException ade) {
            exceptionWasThrown = true;
        }

        assertTrue(exceptionWasThrown);
        verify(applicationService).getApplicationById(application.getId());
    }

    @Test
    public void validApplicationWillResultInRepositoryLookup() {
        lookupWithStatus(RepositoryLookupStatus.SUCCESS);
    }

    @Test(expected = ImageNotFoundException.class)
    public void lookupNotFound() {
        lookupWithStatus(RepositoryLookupStatus.NOT_FOUND);
    }

    @Test(expected = ImageForbiddenException.class)
    public void lookupPermissionDenied() {
        lookupWithStatus(RepositoryLookupStatus.ACCESS_DENIED);
    }

    @Test(expected = ImageLookupException.class)
    public void lookupError() {
        lookupWithStatus(RepositoryLookupStatus.ERROR);
    }

    private void lookupWithStatus(RepositoryLookupStatus status) {
        Application application = prepareValidApplication();
        String imageURI = RandomStringUtils.random(30);

        RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(status);
        when(repositoryOne.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        loadController.load(application.getId(), application.getCode(), "web", null, map("web.url", imageURI));

        verify(repositoryOne, times(1)).getCode();
        verify(repositoryOne).fetchImage(map("url", imageURI));
        verify(repositoryTwo, never()).fetchImage(anyMap());
    }

    @Test(expected = ImageLookupException.class)
    public void noRepositoriesForURI() {
        when(repositoryOne.getCode()).thenReturn("foo");
        when(repositoryTwo.getCode()).thenReturn("bar");

        Application application = prepareValidApplication();
        String imageURI = RandomStringUtils.random(30);

        loadController.load(application.getId(), application.getCode(), "web", null, map("web.url", imageURI));
    }

    @Test
    public void firstRepositoryThatMatchesURIWillBeUsed() {
        when(repositoryOne.getCode()).thenReturn("foo");
        when(repositoryTwo.getCode()).thenReturn("web");

        Application application = prepareValidApplication();
        String imageURI = RandomStringUtils.random(30);

        RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
        when(repositoryTwo.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        loadController.load(application.getId(), application.getCode(), "web", null, map("web.url", imageURI));

        verify(repositoryTwo, times(1)).getCode();
        verify(repositoryOne, never()).fetchImage(anyMap());
        verify(repositoryTwo).fetchImage(map("url", imageURI));
    }

    @Test
    public void newImageWillBeAddedWithDefaultKey() {
        final Application application = prepareValidApplication();
        final String imageURI = RandomStringUtils.random(30);

        final RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
        when(repositoryOne.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        Image expectedImageToSave = new Image();
        expectedImageToSave.setKey(imageURI);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Image actualImage = (Image) invocation.getArguments()[0];
                RepositoryLookupResult actualLookupResult = (RepositoryLookupResult) invocation.getArguments()[1];

                assertNotNull(actualImage);
                assertEquals(imageURI, actualImage.getKey());
                assertEquals(application.getId(), actualImage.getApplicationId());
                assertSame(lookupResult, actualLookupResult);

                return null;
            }
        }).when(imageService).save(any(Image.class), any(RepositoryLookupResult.class));

        loadController.load(application.getId(), application.getCode(), "web", null, map("web.url", imageURI));

        verify(repositoryOne).fetchImage(map("url", imageURI));
        verify(imageService).getImageByKey(imageURI, application.getId());
        verify(imageService).save(any(Image.class), any(RepositoryLookupResult.class));
    }

    @Test
    public void newImageWillBeAddedWithCustomKey() {
        final Application application = prepareValidApplication();
        final String imageURI = RandomStringUtils.random(30);
        final String imageKey = "my-custom-image-key";

        final RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
        when(repositoryOne.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        Image expectedImageToSave = new Image();
        expectedImageToSave.setKey(imageURI);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Image actualImage = (Image) invocation.getArguments()[0];
                RepositoryLookupResult actualLookupResult = (RepositoryLookupResult) invocation.getArguments()[1];

                assertNotNull(actualImage);
                assertEquals(imageKey, actualImage.getKey());
                assertEquals(application.getId(), actualImage.getApplicationId());
                assertSame(lookupResult, actualLookupResult);

                return null;
            }
        }).when(imageService).save(any(Image.class), any(RepositoryLookupResult.class));

        loadController.load(application.getId(), application.getCode(), "web", imageKey, map("web.url", imageURI));

        verify(repositoryOne).fetchImage(map("url", imageURI));
        verify(imageService).getImageByKey(imageKey, application.getId());
        verify(imageService).save(any(Image.class), any(RepositoryLookupResult.class));
    }

    @Test
    public void existingImageWillBeUpdatedWithDefaultKey() {
        final Application application = prepareValidApplication();
        final String imageURI = RandomStringUtils.random(30);

        final RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
        when(repositoryOne.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        final Image existing = new Image();
        when(imageService.getImageByKey(imageURI, application.getId())).thenReturn(existing);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Image actualImage = (Image) invocation.getArguments()[0];
                RepositoryLookupResult actualLookupResult = (RepositoryLookupResult) invocation.getArguments()[1];

                assertSame(existing, actualImage);
                assertSame(lookupResult, actualLookupResult);

                return null;
            }
        }).when(imageService).save(any(Image.class), any(RepositoryLookupResult.class));

        loadController.load(application.getId(), application.getCode(), "web", null, map("web.url", imageURI));

        verify(repositoryOne).fetchImage(map("url", imageURI));
        verify(imageService).getImageByKey(imageURI, application.getId());
        verify(imageService).save(existing, lookupResult);
    }

    @Test
    public void existingImageWillBeUpdatedWithCustomKey() {
        final Application application = prepareValidApplication();
        final String imageURI = RandomStringUtils.random(30);
        final String imageKey = "my-custom-image-key";

        final RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setStatus(RepositoryLookupStatus.SUCCESS);
        when(repositoryOne.fetchImage(map("url", imageURI))).thenReturn(lookupResult);

        final Image existing = new Image();
        when(imageService.getImageByKey(imageKey, application.getId())).thenReturn(existing);

        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Image actualImage = (Image) invocation.getArguments()[0];
                RepositoryLookupResult actualLookupResult = (RepositoryLookupResult) invocation.getArguments()[1];

                assertSame(existing, actualImage);
                assertSame(lookupResult, actualLookupResult);

                return null;
            }
        }).when(imageService).save(any(Image.class), any(RepositoryLookupResult.class));

        loadController.load(application.getId(), application.getCode(), "web", imageKey, map("web.url", imageURI));

        verify(repositoryOne).fetchImage(map("web.url", imageURI));
        verify(imageService).getImageByKey(imageKey, application.getId());
        verify(imageService).save(existing, lookupResult);
    }

    private Application prepareValidApplication() {
        Application application = createApplication(true);
        when(applicationService.getApplicationById(anyInt())).thenReturn(application);
        assertTrue("Precondition on test data failed", application.canBeManaged(application.getCode()));

        return application;
    }

    private Application createApplication(boolean active) {
        Application application = new Application();
        application.setId(RANDOM.nextInt());
        application.setCode(UUID.randomUUID().toString());
        application.setActive(active);

        return application;
    }

    private Map<String, String> map(String key, String value) {
        return Collections.singletonMap(key, value);
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageLookupRepository repositoryOne() {
            return mock(ImageLookupRepository.class);
        }

        @Bean
        public ImageLookupRepository repositoryTwo() {
            return mock(ImageLookupRepository.class);
        }

        @Bean
        public ImageLoadController imageLoadController() {
            return new ImageLoadController();
        }
    }
}
