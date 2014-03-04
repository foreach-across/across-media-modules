package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.data.ImageDao;
import com.foreach.imageserver.core.data.StoredImageModificationDao;
import com.foreach.imageserver.core.services.repositories.RepositoryLookupResult;
import com.foreach.test.MockedLoader;
import org.apache.commons.lang3.RandomStringUtils;
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

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TestImageService.TestConfig.class}, loader = MockedLoader.class)
public class TestImageService {
    @Autowired
    private ImageService imageService;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private ImageTransformService imageTransformService;

    @Autowired
    private TempFileService tempFileService;

    @Autowired
    private ImageDao imageDao;

    @Autowired
    private StoredImageModificationDao modificationDao;

    @Autowired
    private ImageVariantService imageVariantService;

    @Test
    public void getImageByKey() {
        Image expected = new Image();
        when(imageDao.getImageByKey("key", 15)).thenReturn(expected);

        Image image = imageService.getImageByKey("key", 15);
        assertSame(expected, image);
    }

    @Test
    public void saveNewImage() {
        Dimensions dimensions = new Dimensions(1024, 768);

        Image newImage = new Image();
        newImage.setApplicationId(5);
        newImage.setKey(RandomStringUtils.random(50));

        ImageFile tempFile = mock(ImageFile.class);
        InputStream stream = mock(InputStream.class);

        RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setImageType(ImageType.PNG);
        lookupResult.setContent(stream);

        when(tempFileService.createImageFile(ImageType.PNG, stream)).thenReturn(tempFile);
        when(imageTransformService.calculateDimensions(tempFile)).thenReturn(dimensions);

        String expectedPath = RandomStringUtils.randomAlphanumeric(20);
        when(imageStoreService.generateRelativeImagePath(newImage)).thenReturn(expectedPath);
        when(imageStoreService.saveImage(newImage, tempFile)).thenReturn(new ImageFile(null, null, 5678L));

        imageService.save(newImage, lookupResult);

        verify(imageDao, times(1)).insertImage(newImage);
        verify(imageDao, times(1)).updateImage(newImage);
        verify(imageStoreService, never()).deleteVariants(any(Image.class));

        assertEquals(expectedPath, newImage.getFilePath());
        assertEquals(5678, newImage.getFileSize());
        assertEquals(ImageType.PNG, newImage.getImageType());
        assertEquals(dimensions, newImage.getDimensions());
    }

    @Test
    public void updateExistingImage() {
        Dimensions dimensions = new Dimensions(1024, 768);

        String existingPath = RandomStringUtils.randomAlphanumeric(20);

        Image existing = new Image();
        existing.setId(125);
        existing.setApplicationId(5);
        existing.setKey(RandomStringUtils.random(50));
        existing.setFileSize(1000);
        existing.setFilePath(existingPath);
        existing.setDimensions(new Dimensions(1600, 1200));
        existing.setImageType(ImageType.JPEG);

        ImageFile tempFile = mock(ImageFile.class);
        InputStream stream = mock(InputStream.class);

        RepositoryLookupResult lookupResult = new RepositoryLookupResult();
        lookupResult.setImageType(ImageType.PNG);
        lookupResult.setContent(stream);

        when(tempFileService.createImageFile(ImageType.PNG, stream)).thenReturn(tempFile);
        when(imageTransformService.calculateDimensions(tempFile)).thenReturn(dimensions);

        when(imageStoreService.saveImage(existing, tempFile)).thenReturn(new ImageFile(null, null, 5678L));

        imageService.save(existing, lookupResult);

        verify(imageStoreService, never()).generateRelativeImagePath(any(Image.class));
        verify(imageStoreService, times(1)).deleteVariants(existing);
        verify(imageDao, times(1)).updateImage(existing);
        verify(imageDao, never()).insertImage(any(Image.class));

        assertEquals(existingPath, existing.getFilePath());
        assertEquals(5678, existing.getFileSize());
        assertEquals(ImageType.PNG, existing.getImageType());
        assertEquals(dimensions, existing.getDimensions());
    }

    @Test
    public void fetchImageFileThatExists() {
        Image image = new Image();
        image.setDimensions(new Dimensions(100, 200));

        ImageFile imageFile = new ImageFile(ImageType.JPEG, 0, null);
        ImageModification modifier = mock(ImageModification.class);

        ImageModification normalized = new ImageModification();
        normalized.getVariant().setWidth(20);
        normalized.getVariant().setHeight(20);

        when(modifier.normalize(image.getDimensions())).thenReturn(normalized);
        when(imageStoreService.getImageFile(image, normalized)).thenReturn(imageFile);

        ImageFile returned = imageService.fetchImageFile(image, modifier);
        assertSame(imageFile, returned);

        verify(modifier, times(1)).normalize(image.getDimensions());
        verify(imageStoreService, never()).getImageFile(any(Image.class));
        verify(imageTransformService, never()).apply(any(Image.class), any(ImageModification.class));
        verify(imageStoreService, never()).saveImage(any(Image.class), any(ImageModification.class),
                any(ImageFile.class));
    }

    @Test
    public void fetchImageFileThatDoesNotExist() {
        Image image = new Image();
        image.setDimensions(new Dimensions(100, 200));

        ImageModification modifier = mock(ImageModification.class);
        when(modifier.getVariant().isOnlyDimensions()).thenReturn(false);

        ImageModification normalized = new ImageModification();
        normalized.getVariant().setWidth(20);
        normalized.getVariant().setHeight(20);

        ImageFile original = new ImageFile(ImageType.GIF, 0, null);
        ImageFile renderedFile = new ImageFile(ImageType.JPEG, 0, null);
        ImageFile storedFile = new ImageFile(ImageType.PNG, 0, null);

        when(modifier.normalize(image.getDimensions())).thenReturn(normalized);
        when(imageStoreService.getImageFile(image, normalized)).thenReturn(null);
        when(imageStoreService.getImageFile(image)).thenReturn(original);
        when(imageTransformService.apply(image, normalized)).thenReturn(renderedFile);
        when(imageStoreService.saveImage(image, normalized, renderedFile)).thenReturn(storedFile);

        ImageFile returned = imageService.fetchImageFile(image, modifier);

        assertSame(storedFile, returned);
        verify(modificationDao, times(1)).getModification(anyInt(), any(ImageVariant.class));
    }

    @Test
    public void fetchImageFileWithEmptyModifierWithoutExistingModification() {
        Image image = new Image();
        image.setId(123);
        image.setDimensions(new Dimensions(100, 200));

        ImageFile imageFile = new ImageFile(ImageType.JPEG, 0, null);
        ImageFile originalImageFile = new ImageFile(ImageType.JPEG, 0, null);
        ImageModification modifier = mock(ImageModification.class);
        when(modifier.getVariant().isOnlyDimensions()).thenReturn(true);

        ImageModification normalized = new ImageModification();
        normalized.getVariant().setWidth(20);
        normalized.getVariant().setHeight(20);

        when(modifier.normalize(image.getDimensions())).thenReturn(normalized);
        when(imageStoreService.getImageFile(image, normalized)).thenReturn(null);

        when(imageStoreService.getImageFile(image)).thenReturn(originalImageFile);
        when(imageTransformService.apply(image, normalized)).thenReturn(imageFile);
        when(imageStoreService.saveImage(image, normalized, imageFile)).thenReturn(imageFile);

        ImageFile returned = imageService.fetchImageFile(image, modifier);
        assertSame(imageFile, returned);

        verify(modifier, times(1)).normalize(image.getDimensions());
        verify(modificationDao, times(1)).getModification(123, normalized.getVariant());
    }

    @Test
    public void fetchImageFileWillUseExistingModificationIfEmptyModifier() {
        Image image = new Image();
        image.setId(123);
        image.setDimensions(new Dimensions(100, 200));

        ImageFile imageFile = new ImageFile(ImageType.JPEG, 0, null);

        ImageModification modifier = mock(ImageModification.class);
        when(modifier.getVariant().isOnlyDimensions()).thenReturn(true);

        ImageModification normalized = new ImageModification();
        normalized.getVariant().setWidth(20);
        normalized.getVariant().setHeight(20);

        when(modifier.normalize(image.getDimensions())).thenReturn(normalized);
        when(imageStoreService.getImageFile(image, normalized)).thenReturn(null);

        StoredImageModification modification = mock(StoredImageModification.class);
        when(modificationDao.getModification(image.getId(), normalized.getVariant())).thenReturn(modification);

        ImageModification registeredModifier = mock(ImageModification.class);
        when(modification.getModification()).thenReturn(registeredModifier);
        when(registeredModifier.normalize(any(Dimensions.class))).thenReturn(registeredModifier);

        when(imageStoreService.getImageFile(image, registeredModifier)).thenReturn(imageFile);

        ImageFile returned = imageService.fetchImageFile(image, modifier);
        assertSame(imageFile, returned);

        verify(modifier, times(1)).normalize(image.getDimensions());
        verify(modificationDao, times(1)).getModification(123, normalized.getVariant());
        verify(imageStoreService, never()).getImageFile(any(Image.class));
        verify(imageTransformService, never()).apply(any(Image.class), any(ImageModification.class));
        verify(imageStoreService, never()).saveImage(any(Image.class), any(ImageModification.class),
                any(ImageFile.class));
    }

    @Test
    public void ifModificationIsNotYetCreatedItWillBeCreatedAsRequestedModifier() {
        Image image = new Image();
        image.setId(123);
        image.setDimensions(new Dimensions(100, 200));

        ImageFile imageFile = new ImageFile(ImageType.JPEG, 0, null);
        ImageFile originalImageFile = new ImageFile(ImageType.JPEG, 0, null);

        ImageModification modifier = mock(ImageModification.class);
        when(modifier.getVariant().isOnlyDimensions()).thenReturn(true);

        ImageModification normalized = new ImageModification();
        normalized.getVariant().setWidth(20);
        normalized.getVariant().setHeight(20);

        when(modifier.normalize(image.getDimensions())).thenReturn(normalized);
        when(imageStoreService.getImageFile(image, normalized)).thenReturn(null);

        StoredImageModification modification = mock(StoredImageModification.class);
        when(modificationDao.getModification(image.getId(), normalized.getVariant())).thenReturn(modification);

        ImageModification registeredModifier = mock(ImageModification.class);
        when(modification.getModification()).thenReturn(registeredModifier);
        when(registeredModifier.normalize(any(Dimensions.class))).thenReturn(registeredModifier);

        when(imageStoreService.getImageFile(image)).thenReturn(originalImageFile);
        when(imageTransformService.apply(image, registeredModifier)).thenReturn(imageFile);
        when(imageStoreService.saveImage(image, normalized, imageFile)).thenReturn(imageFile);

        ImageFile returned = imageService.fetchImageFile(image, modifier);
        assertSame(imageFile, returned);
    }

    @Test
    public void deleteImageAndAllVariants() {
        Image image = new Image();
        image.setId(123);

        imageService.delete(image, false);

        verify(imageDao, times(1)).deleteImage(image.getId());
        verify(imageStoreService, times(1)).delete(image);
    }

    @Test
    public void deleteImageVariants() {
        Image image = new Image();
        image.setId(123);

        imageService.delete(image, true);

        verify(imageDao, never()).deleteImage(image.getId());
        verify(imageStoreService, never()).delete(image);
        verify(imageStoreService, times(1)).deleteVariants(image);
    }

    @Test
    public void registerNewImageModification() {
        Image image = new Image();
        image.setId(123);

        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setWidth(800);
        imageVariant.setHeight(600);

        final ImageModification modifier = mock(ImageModification.class);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                StoredImageModification modification = (StoredImageModification) invocation.getArguments()[0];
                assertEquals(123, modification.getImageId());
                assertSame(modifier, modification.getModification());
                return null;
            }
        }).when(modificationDao).insertModification(any(StoredImageModification.class));

        imageVariantService.registerVariant(image, modifier);

        verify(modificationDao, times(1)).getModification(123, imageVariant);
        verify(modificationDao, times(1)).insertModification(any(StoredImageModification.class));
    }

    @Test
    public void updateExistingModification() {
        Image image = new Image();
        image.setId(123);

        ImageVariant imageVariant = new ImageVariant();
        imageVariant.setHeight(800);
        imageVariant.setWidth(600);

        final Dimensions dimensions = mock(Dimensions.class);
        final ImageModification modifier = mock(ImageModification.class);

        final StoredImageModification existing = new StoredImageModification();

        when(dimensions.normalize(any(Dimensions.class))).thenReturn(new Dimensions(800, 600));
        when(modificationDao.getModification(123, imageVariant)).thenReturn(existing);

        imageVariantService.registerVariant(image, modifier);

        verify(modificationDao, times(1)).getModification(123, imageVariant);
        verify(modificationDao, never()).insertModification(any(StoredImageModification.class));
        verify(modificationDao, times(1)).updateModification(existing);

        assertSame(modifier, existing.getModification());
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageService imageService() {
            return new ImageServiceImpl();
        }
    }
}
