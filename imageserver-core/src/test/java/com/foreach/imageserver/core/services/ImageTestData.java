package com.foreach.imageserver.core.services;

import com.foreach.imageserver.core.business.Dimensions;
import com.foreach.imageserver.core.business.ImageFile;
import com.foreach.imageserver.core.business.ImageType;

import java.io.InputStream;

public enum ImageTestData {
    SUNSET("/images/sunset.jpg", "image/jpeg", ImageType.JPEG, false, 1083349L, 1420, 930),
    EARTH("/images/earth_large.jpg", "image/jpeg", ImageType.JPEG, false, 11803928L, 9104, 6828),
    HIGH_RES("/images/high_res_conversion.jpeg", "image/jpeg", ImageType.JPEG, false, 4569331L, 3072, 2048),
    CMYK_COLOR("/images/CMYK_Error_COLOR_ID.jpg", "image/jpeg", ImageType.JPEG, false, 192947L, 679, 602),
    ICE_ROCK("/images/icerock.jpeg", "image/jpeg", ImageType.JPEG, false, 138605L, 1600, 1200),
    KAAIMAN_JPEG("/images/kaaimangrootkleur.jpg", "image/jpeg", ImageType.JPEG, false, 166746L, 900, 900),
    KAAIMAN_GIF("/images/kaaimangrootkleur.gif", "image/gif", ImageType.GIF, true, 152568L, 900, 900),
    KAAIMAN_PNG("/images/kaaimangrootkleur.png", "image/png", ImageType.PNG, true, 436783L, 900, 900),
    KAAIMAN_SVG("/images/kaaimangrootkleur.svg", "image/svg+xml", ImageType.SVG, true, 410672L, 900, 900),
    KAAIMAN_EPS("/images/kaaimangrootkleur.eps", "image/x-eps", ImageType.EPS, true, 410057L, 900, 900),
    TEST_PNG("/images/test.png", "image/png", ImageType.PNG, true, 163L, 100, 55),
    TEST_EPS("/images/test.eps", "application/eps", ImageType.EPS, false, 1521754L, 321, 583),
    TEST_TRANSPARENT_PNG("/images/test.transparent.png", "image/png", ImageType.PNG, true, 145620L, 800, 415),
    BRUXELLES_EPS("/images/BRUXELLES_RGB.eps", "image/eps", ImageType.EPS, false, 89228L, 130, 104),
    BRUXELLES_ECHO_EPS("/images/BRUXELLES_ECHO.eps", "application/postscript", ImageType.EPS, false, 838966L, 130,
            104),
    SINGLE_PAGE_PDF("/images/test_singlepage.pdf", "application/pdf", ImageType.PDF, false, 965622L, 1469, 1016),
    MULTI_PAGE_PDF("/images/test_multipage.pdf", "application/x-pdf", ImageType.PDF, false, 7013693L, 893, 1332),
    SMALL_TIFF("/images/small.tif", "image/tiff", ImageType.TIFF, false, 397012L, 1375, 1375),
    LARGE_TIFF("/images/large.tif", "image/tiff", ImageType.TIFF, false, 60600776L, 6400, 6400),
    HUGE_TIFF("/images/huge.tif", "image/tiff", ImageType.TIFF, false, 73292814L, 4288, 2848),
    ANIMATED_GIF("/images/animated.gif", "image/gif", ImageType.GIF, true, 144124L, 350, 350);

    private final String resourcePath, contentType;
    private final ImageType imageType;
    private final long fileSize;
    private final Dimensions dimensions;
    private final boolean transparent;

    ImageTestData(String resourcePath,
                  String contentType,
                  ImageType imageType,
                  boolean transparent,
                  long fileSize,
                  int width,
                  int height) {
        this.resourcePath = resourcePath;
        this.contentType = contentType;
        this.imageType = imageType;
        this.fileSize = fileSize;
        this.dimensions = new Dimensions(width, height);
        this.transparent = transparent;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public String getContentType() {
        return contentType;
    }

    public ImageType getImageType() {
        return imageType;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public InputStream getResourceAsStream() {
        return getClass().getResourceAsStream(getResourcePath());
    }

    public long getFileSize() {
        return fileSize;
    }

    public Dimensions getDimensions() {
        return dimensions;
    }

    public ImageFile getImageFile() {
        return new ImageFile(imageType, fileSize, getResourceAsStream());
    }
}
