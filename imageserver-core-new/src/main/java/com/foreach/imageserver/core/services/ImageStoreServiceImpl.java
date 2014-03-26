package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.*;
import com.foreach.imageserver.core.transformers.StreamImageSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
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
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermissions;

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
    private final Path variantsFolder;

    private final FileAttribute[] folderAttributes;
    private final FileAttribute[] fileAttributes;

    @Autowired
    public ImageStoreServiceImpl(@Value("${imagestore.folder}") File imageStoreFolder,
                                 @Value("${imagestore.permissions.folders}") String folderPermissions,
                                 @Value("${imagestore.permissions.files}") String filePermissions) throws IOException {
        folderAttributes = toFileAttributes(folderPermissions);
        fileAttributes = toFileAttributes(filePermissions);

        Path imageStoreFolderPath = imageStoreFolder.toPath();

        tempFolder = imageStoreFolderPath.resolve("temp");
        originalsFolder = imageStoreFolderPath.resolve("originals");
        variantsFolder = imageStoreFolderPath.resolve("variants");

        Files.createDirectories(tempFolder, folderAttributes);
        Files.createDirectories(originalsFolder, folderAttributes);
        Files.createDirectories(variantsFolder, folderAttributes);
    }

    @Override
    public void storeOriginalImage(ImageParameters imageParameters, byte[] imageBytes) {
        InputStream imageStream = null;
        try {
            imageStream = new ByteArrayInputStream(imageBytes);
            this.storeOriginalImage(imageParameters, imageStream);
        } finally {
            IOUtils.closeQuietly(imageStream);
        }
    }

    @Override
    public void storeOriginalImage(ImageParameters imageParameters, InputStream imageStream) {
        Path targetPath = setupTargetPath(imageParameters);
        writeSafely(imageStream, targetPath);
    }

    @Override
    public StreamImageSource getOriginalImage(ImageParameters imageParameters) {
        Path targetPath = setupTargetPath(imageParameters);
        return read(targetPath, imageParameters.getImageType());
    }


    @Override
    public void storeVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant, InputStream imageStream) {
        Path targetPath = setupTargetPath(image, context, imageResolution, imageVariant);
        writeSafely(imageStream, targetPath);
    }

    @Override
    public StreamImageSource getVariantImage(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        Path targetPath = setupTargetPath(image, context, imageResolution, imageVariant);
        return read(targetPath, imageVariant.getOutputType());
    }

    private Path setupTargetPath(ImageParameters imageParameters) {
        try {
            String fileName = imageParameters.getUniqueFileName();
            String repositoryCode = imageParameters.getRepositoryCode();

            Path targetFolder = originalsFolder.resolve(repositoryCode);
            Files.createDirectories(targetFolder, folderAttributes);

            return targetFolder.resolve(fileName);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private Path setupTargetPath(Image image, Context context, ImageResolution imageResolution, ImageVariant imageVariant) {
        try {
            String fileName = constructFileName(image, imageResolution, imageVariant);

            Path targetFolder = variantsFolder.resolve(context.getCode());
            Files.createDirectories(targetFolder, folderAttributes);

            return targetFolder.resolve(fileName);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private String constructFileName(Image image, ImageResolution imageResolution, ImageVariant imageVariant) {
        StringBuilder fileNameBuilder = new StringBuilder();
        fileNameBuilder.append(image.getImageId());
        fileNameBuilder.append('-');
        if (imageResolution.getWidth() != null) {
            fileNameBuilder.append('w');
            fileNameBuilder.append(imageResolution.getWidth());
            fileNameBuilder.append('-');
        }
        if (imageResolution.getHeight() != null) {
            fileNameBuilder.append('h');
            fileNameBuilder.append(imageResolution.getHeight());
        }
        fileNameBuilder.append('.');
        fileNameBuilder.append(imageVariant.getOutputType().getExtension());

        return fileNameBuilder.toString();
    }

    private void writeSafely(InputStream inputStream, Path targetPath) {
        try {
            Path temporaryPath = Files.createTempFile(tempFolder, "image", ".tmp", fileAttributes);
            Files.copy(inputStream, temporaryPath, StandardCopyOption.REPLACE_EXISTING);
            Files.move(temporaryPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new ImageStoreException(e);
        }
    }

    private StreamImageSource read(Path targetPath, ImageType imageType) {
        // Do not use .exists() kind of logic here! Open the file and check for an exception instead.
        // This will ensure that we can read the full file contents, even should the file be deleted or replaced while
        // we are busy with it.

        InputStream imageStream = null;
        try {
            imageStream = Files.newInputStream(targetPath);
        } catch (IOException e) {
            // Let imageStream be null.
        }

        StreamImageSource imageSource = null;
        if (imageStream != null) {
            imageSource = new StreamImageSource(imageType, imageStream);
        }

        return imageSource;
    }

    private FileAttribute[] toFileAttributes(String permissions) {
        FileAttribute[] attributes = null;
        if (StringUtils.isNotBlank(permissions)) {
            FileAttribute attribute = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString(permissions));
            attributes = new FileAttribute[1];
            attributes[0] = attribute;
        } else {
            attributes = new FileAttribute[0];
        }
        return attributes;
    }

}
