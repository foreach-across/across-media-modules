package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.DateUtils;
import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.exceptions.ImageStoreOperationException;
import com.foreach.test.MockedLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {TestImageStoreService.TestConfig.class}, loader = MockedLoader.class)
public class TestImageStoreService {
    private static final Logger LOG = LoggerFactory.getLogger(TestImageStoreService.class);

    private static String TEMP_DIR;
    private static String ORIGINAL_STORE;
    private static String VARIANT_STORE;

    @Autowired
    private ImageStoreService imageStoreService;

    @Autowired
    private TempFileService tempFileService;

    @BeforeClass
    public static void createStorePaths() {
        TEMP_DIR =
                System.getProperty("java.io.tmpdir") + "/imgsrv-" + RandomStringUtils.randomAlphanumeric(15) + "/";

        assertFalse("Temp image store directory should not yet exist", new File(TEMP_DIR).exists());
        ORIGINAL_STORE = TEMP_DIR + "original";
        VARIANT_STORE = TEMP_DIR + "/variant";
    }

    @AfterClass
    public static void cleanupStorePaths() {
        try {
            FileUtils.deleteDirectory(new File(TEMP_DIR));
        } catch (IOException ioe) {
            LOG.warn("Could not cleanup unit test path: ", ioe);
        }
    }

    @Test
    public void generateRelativeImagePath() {
        Image image = new Image();
        image.setDateCreated(DateUtils.parseDate("2013-07-06 13:35:13"));

        String path = imageStoreService.generateRelativeImagePath(image);

        assertEquals("/2013/07/06/", path);
    }

    @Test
    public void saveNewImage() throws Exception {
        Image image = new Image();
        image.setId(2);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/09/");
        image.setImageType(ImageType.JPEG);

        saveAndVerify(image, null, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/2.jpeg", false);
        saveAndVerify(image, null, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/2.jpeg", true);
    }

    @Test
    public void updateImage() throws Throwable {
        Image image = new Image();
        image.setId(3);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/09/");
        image.setImageType(ImageType.JPEG);

        saveAndVerify(image, null, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg", false);
        saveAndVerify(image, null, ImageTestData.EARTH, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg", true);
        saveAndVerify(image, null, ImageTestData.SUNSET, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg", false);
        saveAndVerify(image, null, ImageTestData.SUNSET, ORIGINAL_STORE, "/10/2013/07/09/3.jpeg", true);
    }

    @Test
    public void saveNewVariantImage() throws Exception {
        Image image = new Image();
        image.setId(6);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(1600);
        modifier.getModifier().setHeight(200);

        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/6.1600x200.jpeg", false);
        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/6.1600x200.jpeg", true);

        modifier.getModifier().setWidth(0);
        saveAndVerify(image, modifier, ImageTestData.EARTH, VARIANT_STORE, "/10/2013/07/06/6.0x200.jpeg", false);
        saveAndVerify(image, modifier, ImageTestData.EARTH, VARIANT_STORE, "/10/2013/07/06/6.0x200.jpeg", true);

        modifier.getModifier().setOutput(ImageType.PNG);
        modifier.setCrop(new Crop(10, 5, 50, 75));
        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/6.0x200.[50x75+10+5].png",
                false);
        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/6.0x200.[50x75+10+5].png",
                true);
    }

    @Test
    public void updateVariantImage() throws Exception {
        Image image = new Image();
        image.setId(7);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(1600);
        modifier.getModifier().setHeight(200);

        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/7.1600x200.jpeg", false);
        saveAndVerify(image, modifier, ImageTestData.SUNSET, VARIANT_STORE, "/10/2013/07/06/7.1600x200.jpeg", true);
        saveAndVerify(image, modifier, ImageTestData.EARTH, VARIANT_STORE, "/10/2013/07/06/7.1600x200.jpeg", false);
        saveAndVerify(image, modifier, ImageTestData.EARTH, VARIANT_STORE, "/10/2013/07/06/7.1600x200.jpeg", true);
    }

    private void saveAndVerify(Image image,
                               ImageVariant modifier,
                               ImageTestData testData,
                               String path,
                               String expectedFileName,
                               boolean useTempFile) throws Exception {
        reset(tempFileService);

        InputStream imageData = testData.getResourceAsStream();

        File expectedFile = new File(path, expectedFileName);

        ImageFile savedImageFile;

        ImageType imageTypeToSave = StringUtils.endsWith(expectedFileName, "png") ? ImageType.PNG : ImageType.JPEG;
        ImageFile imageFileToSave = new ImageFile(imageTypeToSave, testData.getFileSize(), imageData);

        when(tempFileService.isTempFile(imageFileToSave)).thenReturn(useTempFile);
        when(tempFileService.move(imageFileToSave, expectedFile)).thenReturn(
                new ImageFile(imageTypeToSave, testData.getFileSize(), imageData));

        if (modifier != null) {
            savedImageFile = imageStoreService.saveImage(image, modifier, imageFileToSave);
        } else {
            savedImageFile = imageStoreService.saveImage(image, imageFileToSave);
        }

        verify(tempFileService, times(1)).isTempFile(imageFileToSave);
        if (!useTempFile) {
            verify(tempFileService, never()).move(any(ImageFile.class), any(File.class));
        } else {
            verify(tempFileService, times(1)).move(imageFileToSave, expectedFile);
        }

        assertEquals(imageTypeToSave, savedImageFile.getImageType());
        assertEquals(testData.getFileSize(), savedImageFile.getFileSize());
        assertTrue(expectedFile.exists());

        FileInputStream fos = new FileInputStream(expectedFile);
        assertTrue(IOUtils.contentEquals(testData.getResourceAsStream(), fos));
        fos.close();

        InputStream ios = savedImageFile.openContentStream();
        assertTrue(IOUtils.contentEquals(testData.getResourceAsStream(), ios));
        ios.close();
    }

    @Test(expected = ImageStoreOperationException.class)
    public void failSavingOriginal() throws Exception {
        Image image = new Image();
        image.setId(2);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/09/");
        image.setImageType(ImageType.JPEG);

        imageStoreService.saveImage(image, null);
    }

    @Test
    public void deleteVariants() throws Exception {
        Image image = new Image();
        image.setId(2);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        // Put dummy files in place
        new File(VARIANT_STORE, "/10/2013/07/06/").mkdirs();
        new File(ORIGINAL_STORE, "/10/2013/07/06/").mkdirs();

        File original = createDummy(ORIGINAL_STORE, "/10/2013/07/06/2.jpeg");
        File variantOne = createDummy(VARIANT_STORE, "/10/2013/07/06/2.100x200.jpeg");
        File variantTwo = createDummy(VARIANT_STORE, "/10/2013/07/06/2.test.png");
        File otherVariant = createDummy(VARIANT_STORE, "/10/2013/07/06/3.100x200.jpeg");

        assertTrue(original.exists());
        assertTrue(variantOne.exists());
        assertTrue(variantTwo.exists());
        assertTrue(otherVariant.exists());

        imageStoreService.deleteVariants(image);

        assertTrue(original.exists());
        assertFalse(variantOne.exists());
        assertFalse(variantTwo.exists());
        assertTrue(otherVariant.exists());
    }

    @Test
    public void deleteImage() throws Exception {
        Image image = new Image();
        image.setId(2);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/10/");
        image.setImageType(ImageType.JPEG);

        // Put dummy files in place
        new File(VARIANT_STORE, "/10/2013/07/10/").mkdirs();
        new File(ORIGINAL_STORE, "/10/2013/07/10/").mkdirs();

        File original = createDummy(ORIGINAL_STORE, "/10/2013/07/10/2.jpeg");
        File variantOne = createDummy(VARIANT_STORE, "/10/2013/07/10/2.100x200.jpeg");
        File variantTwo = createDummy(VARIANT_STORE, "/10/2013/07/10/2.test.png");
        File otherOriginal = createDummy(ORIGINAL_STORE, "/10/2013/07/10/7.jpeg");
        File otherVariant = createDummy(VARIANT_STORE, "/10/2013/07/10/3.100x200.jpeg");

        assertTrue(original.exists());
        assertTrue(variantOne.exists());
        assertTrue(variantTwo.exists());
        assertTrue(otherOriginal.exists());
        assertTrue(otherVariant.exists());

        imageStoreService.delete(image);

        assertFalse(original.exists());
        assertTrue(otherOriginal.exists());
        assertFalse(variantOne.exists());
        assertFalse(variantTwo.exists());
        assertTrue(otherVariant.exists());
    }

    private File createDummy(String path, String fileName) throws Exception {
        File file = new File(path, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.write(new byte[0], fos);
        fos.close();

        return file;
    }

    @Test
    public void originalImagePathGeneration() throws Exception {
        Image image = new Image();
        image.setId(3);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        String expected = new File(ORIGINAL_STORE, "/10/2013/07/06/3.jpeg").getAbsolutePath();

        assertEquals(expected, imageStoreService.generateFullImagePath(image));
        assertEquals(expected, imageStoreService.generateFullImagePath(image, null));
        assertEquals(expected, imageStoreService.generateFullImagePath(image, new ImageVariant()));
    }

    @Test
    public void variantImagePathGeneration() throws Exception {
        Image image = new Image();
        image.setId(3);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        String root = new File(VARIANT_STORE, "/10/2013/07/06/").getAbsolutePath();

        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setWidth(1600);
        assertEquals(variant(root, "3.1600x0.jpeg"), imageStoreService.generateFullImagePath(image, modifier));

        modifier.getModifier().setHeight(1200);
        assertEquals(variant(root, "3.1600x1200.jpeg"), imageStoreService.generateFullImagePath(image, modifier));
    }

    private String variant(String path, String fileName) {
        return new File(path, fileName).getAbsolutePath();
    }

    @Test
    public void getOriginalImageFile() throws Exception {
        Image image = new Image();
        image.setId(3);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        new File(ORIGINAL_STORE, "/10/2013/07/06/").mkdirs();

        File actual = createActual(ORIGINAL_STORE, "/10/2013/07/06/3.jpeg", ImageTestData.SUNSET);

        verifyImageFile(imageStoreService.getImageFile(image), ImageType.JPEG, ImageTestData.SUNSET, actual);
        verifyImageFile(imageStoreService.getImageFile(image, null), ImageType.JPEG, ImageTestData.SUNSET, actual);
        verifyImageFile(imageStoreService.getImageFile(image, new ImageVariant()), ImageType.JPEG,
                ImageTestData.SUNSET, actual);
    }

    @Test
    public void getVariantImageFile() throws Exception {
        Image image = new Image();
        image.setId(3);
        image.setApplicationId(10);
        image.setFilePath("/2013/07/06/");
        image.setImageType(ImageType.JPEG);

        ImageVariant modifier = new ImageVariant();
        modifier.getModifier().setOutput(ImageType.PNG);
        modifier.getModifier().setWidth(1600);
        modifier.getModifier().setHeight(200);

        new File(VARIANT_STORE, "/10/2013/07/06/").mkdirs();

        File actual = createActual(VARIANT_STORE, "/10/2013/07/06/3.1600x200.png", ImageTestData.SUNSET);

        verifyImageFile(imageStoreService.getImageFile(image, modifier), ImageType.PNG, ImageTestData.SUNSET, actual);
    }

    private File createActual(String path, String fileName, ImageTestData testData) throws Exception {
        File file = new File(path, fileName);
        FileOutputStream fos = new FileOutputStream(file);
        IOUtils.copy(testData.getResourceAsStream(), fos);
        fos.close();

        return file;
    }

    private void verifyImageFile(ImageFile imageFile,
                                 ImageType expectedType,
                                 ImageTestData testData,
                                 File physical) throws IOException {
        assertNotNull(imageFile);
        assertEquals(expectedType, imageFile.getImageType());
        assertEquals(testData.getFileSize(), imageFile.getFileSize());
        FileInputStream fos = new FileInputStream(physical);
        assertTrue(IOUtils.contentEquals(testData.getResourceAsStream(), fos));
        fos.close();
        imageFile.openContentStream().close();
    }

    @Configuration
    public static class TestConfig {
        @Bean
        public ImageStoreService imageStoreService() {
            return new ImageStoreServiceImpl(ORIGINAL_STORE, VARIANT_STORE);
        }
    }
}
