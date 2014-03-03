package com.foreach.imageserver.core.services.transformers;

import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageModifier;
import com.foreach.imageserver.core.services.ImageTestData;
import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.im4java.core.Info;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

public abstract class AbstractImageTransformerTest {
    protected Logger LOG;
    protected ImageTransformer transformer;

    @Before
    public void setup() {
        LOG = LoggerFactory.getLogger(getClass());
        transformer = createTransformer();
    }

    protected void dimensions(ImageTestData image, ImageTransformerPriority expectedPriority, boolean shouldSucceed) {
        long start = System.currentTimeMillis();

        ImageCalculateDimensionsAction action = new ImageCalculateDimensionsAction(image.getImageFile());

        ImageTransformerPriority priority = transformer.canExecute(action);
        assertEquals("Wrong priority for calculating dimensions for " + image.getResourcePath(), expectedPriority,
                priority);

        if (priority != ImageTransformerPriority.UNABLE) {
            boolean succeeded = true;
            try {
                transformer.execute(action);
                assertEquals("Calculated dimensions for " + image.getResourcePath() + " are wrong",
                        image.getDimensions(), action.getResult());

                LOG.debug("Dimensions calculated for {} in {} ms", image.getResourcePath(),
                        (System.currentTimeMillis() - start));
            } catch (Exception e) {
                if (shouldSucceed) {
                    LOG.error("Exception calculating dimensions for " + image.getResourcePath(), e);
                }
                succeeded = false;
            }

            assertEquals("Unexpected outcome of calculating dimensions for " + image.getResourcePath(), shouldSucceed,
                    succeeded);
        }
    }

    protected void modify(String label,
                          ImageTestData image,
                          ImageModifier modifier,
                          ImageTransformerPriority expectedPriority,
                          boolean shouldSucceed) {
        long start = System.currentTimeMillis();

        ImageModifier normalized = modifier.normalize(image.getDimensions());
        ImageModifyAction action = new ImageModifyAction(image.getImageFile(), normalized);

        ImageTransformerPriority priority = transformer.canExecute(action);
        assertEquals("Wrong priority for image modification " + normalized + " for " + image.getResourcePath(),
                expectedPriority, priority);

        if (priority != ImageTransformerPriority.UNABLE) {
            boolean succeeded = true;
            try {
                transformer.execute(action);

                ImageFile modified = action.getResult();
                assertNotNull(modified);

                LOG.debug("Applied modification {} to {} in {} ms", normalized, image.getResourcePath(),
                        (System.currentTimeMillis() - start));

                File dir = new File("target/test-images/" + image.name() + "/");

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File output = new File(dir,
                        image.name() + "." + label + "." + transformer.getName() + "." + normalized.getOutput().getExtension());

                FileOutputStream fos = new FileOutputStream(output);
                IOUtils.copy(modified.openContentStream(), fos);
                IOUtils.closeQuietly(fos);

                verifyUsingImageMagickThatImageMatchesModifier(output, image, normalized);
            } catch (Exception e) {
                if (shouldSucceed) {
                    LOG.error("Exception applying modification " + normalized + " to " + image.getResourcePath(), e);
                }
                succeeded = false;
            }

            assertEquals("Unexpected modification outcome for " + normalized + " on " + image.getResourcePath(),
                    shouldSucceed, succeeded);
        }
    }

    private void verifyUsingImageMagickThatImageMatchesModifier(File file,
                                                                ImageTestData image,
                                                                ImageModifier modifier) throws Exception {
        Info info = new Info(file.getAbsolutePath(), true);
        assertEquals(modifier.getWidth(), info.getImageWidth());
        assertEquals(modifier.getHeight(), info.getImageHeight());
        assertEquals(StringUtils.upperCase(modifier.getOutput().getExtension()), info.getImageFormat());

        if (image.isTransparent() && modifier.getOutput().hasTransparency()) {
            verifyTransparency(file);
        }
    }

    private void verifyTransparency(File file) throws Exception {
        ImageInfo info = Imaging.getImageInfo(file);
        assertTrue("File " + file.getAbsolutePath() + " was expected to be transparent", info.isTransparent());
    }


    protected abstract ImageTransformer createTransformer();
}
