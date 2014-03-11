/*
package com.foreach.imageserver.core.integrationtests;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.services.transformers.ImageMagickImageTransformer;
import com.foreach.imageserver.core.services.transformers.ImageModifyAction;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CropPerformanceTest extends AbstractIntegrationTest {

    @Autowired
    private ImageMagickImageTransformer imageTransformer;

    @Test
    @Ignore
    public void runTest() throws InterruptedException {
        final int nrOfRuns = 5;
        final int nrOfCropsPerRun = 150;
        final int nrOfThreads = 20;

        final Runnable performCropRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    performSingleCrop();
                } catch (IOException e) {
                }
            }
        };

        for (int i = 0; i < nrOfRuns; ++i) {
            System.gc();
            Thread.sleep(5000);

            System.out.println("Begin run.");

            long startTime = System.currentTimeMillis();

            ExecutorService threadPool = Executors.newFixedThreadPool(nrOfThreads);
            for (int j = 0; j < nrOfCropsPerRun; ++j) {
                threadPool.execute(performCropRunnable);
            }
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.HOURS);

            long endTime = System.currentTimeMillis();

            System.out.println("End run. " + ((double) (endTime - startTime) / 1000.0));
            Thread.sleep(5000);
        }
    }

    private void performSingleCrop() throws IOException {
        ImageFile imageFile = new ImageFile(ImageType.PNG, 0, resourceStream("images/cropCorrectness.png"));
        ImageModification modification = new ImageModification(
                variant(800, 800, ImageType.PNG),
                crop(620, 120, 800, 800, 3000, 3000));
        ImageModifyAction action = new ImageModifyAction(imageFile, modification);

        imageTransformer.execute(action);
        IOUtils.closeQuietly(action.getResult().openContentStream());
    }

    private InputStream resourceStream(String uri) {
        return getClass().getClassLoader().getResourceAsStream(uri);
    }

    private ImageVariant variant(int w, int h, ImageType type) {
        ImageVariant variant = new ImageVariant();
        variant.setWidth(w);
        variant.setHeight(h);
        variant.setOutput(type);
        return variant;
    }

    private Crop crop(int x, int y, int w, int h, int ow, int oh) {
        return new Crop(x, y, w, h, ow, oh);
    }

}
*/