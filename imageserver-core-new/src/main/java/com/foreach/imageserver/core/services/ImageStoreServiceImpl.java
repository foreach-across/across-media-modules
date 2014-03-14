package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.OriginalImage;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Still to verify and/or implement:
 * - Files.createTempPath needs to be atomic.
 * - Files.copy with option REPLACE_EXISTING should never cause the temp file to not exist.
 * - We should not store all images in one big folder. A hashing mechanism should be set up for this.
 * <p/>
 * TODO Resolve the above.
 */
@Service
public class ImageStoreServiceImpl implements ImageStoreService {
    private final Path tempFolder;
    private final Path originalsFolder;

    @Autowired
    public ImageStoreServiceImpl(@Value("${imagestore.folder}") File imageStoreFolder) throws IOException {
        Path imageStoreFolderPath = imageStoreFolder.toPath();

        tempFolder = imageStoreFolderPath.resolve("temp");
        originalsFolder = imageStoreFolderPath.resolve("originals");

        Files.createDirectories(tempFolder);
        Files.createDirectories(originalsFolder);
    }

    @Override
    public void storeOriginalImage(OriginalImage originalImage, byte[] imageBytes) {
        InputStream imageStream = null;
        try {
            imageStream = new ByteArrayInputStream(imageBytes);
            this.storeOriginalImage(originalImage, imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    @Override
    public void storeOriginalImage(OriginalImage originalImage, InputStream imageStream) {
        Path targetPath = setupTargetPath(originalImage);
        writeSafely(imageStream, targetPath);
    }

    private Path setupTargetPath(OriginalImage originalImage) {
        try {
            String fileName = originalImage.getUniqueFileName();
            String repositoryCode = originalImage.getRepositoryCode();

            Path targetFolder = originalsFolder.resolve(repositoryCode);
            Files.createDirectories(targetFolder);

            return targetFolder.resolve(fileName);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private void writeSafely(InputStream inputStream, Path targetPath) {
        try {
            Path temporaryPath = Files.createTempFile(tempFolder, "image", ".tmp");
            Files.copy(inputStream, temporaryPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporaryPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }
}
