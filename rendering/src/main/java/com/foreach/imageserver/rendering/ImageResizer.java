package com.foreach.imageserver.rendering;

import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;

import java.io.IOException;

public interface ImageResizer {

    String version() throws IOException, InterruptedException;

    Size getSize( String filePath ) throws IOException, InterruptedException;

    void resize( String srcPath, String targetPath, Size targetSize, Rect cropRect  )
            throws IOException, InterruptedException, ScaleException;
}
