package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.StoredImageModificationDao;
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
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TestImageModificationService.TestConfig.class}, loader = MockedLoader.class)
public class TestImageModificationService {

    @Autowired
    private StoredImageModificationDao mockedModificationDao;

    @Autowired
    private ImageModificationService imageModificationService;

    @Test
    public void registerNewImageModification() {
        Image image = new Image();
        image.setId(123);

        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setWidth(800);
        imageVariant.setHeight(600);

        Crop crop = new Crop(100, 100, 100, 100);

        final ImageModification modifier = new ImageModification(imageVariant, crop);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                StoredImageModification modification = (StoredImageModification) invocation.getArguments()[0];
                assertEquals(123, modification.getImageId());
                assertSame(modifier, modification.getModification());
                return null;
            }
        }).when(mockedModificationDao).insertModification(any(StoredImageModification.class));

        imageModificationService.saveModification(image, modifier);

        verify(mockedModificationDao, times(1)).getModification(123, imageVariant);
        verify(mockedModificationDao, times(1)).insertModification(any(StoredImageModification.class));
    }

    @Test
    public void updateExistingModification() {
        Image image = new Image();
        image.setId(123);

        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setHeight(800);
        imageVariant.setWidth(600);

        Crop crop = new Crop(100, 100, 100, 100);

        final ImageModification modifier = new ImageModification(imageVariant, crop);

        final Dimensions dimensions = mock(Dimensions.class);

        final StoredImageModification existing = new StoredImageModification();

        when(dimensions.normalize(any(Dimensions.class))).thenReturn(new Dimensions(800, 600));
        when(mockedModificationDao.getModification(123, imageVariant)).thenReturn(existing);

        imageModificationService.saveModification(image, modifier);

        verify(mockedModificationDao, times(1)).getModification(123, imageVariant);
        verify(mockedModificationDao, never()).insertModification(any(StoredImageModification.class));
        verify(mockedModificationDao, times(1)).updateModification(existing);

        assertSame(modifier, existing.getModification());
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageModificationService imageService() {
            return new ImageModificationServiceImpl();
        }
    }
}
