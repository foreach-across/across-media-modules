package com.foreach.imageserver.rendering;


import com.foreach.imageserver.business.geometry.Rect;
import com.foreach.imageserver.business.geometry.Size;

import java.io.*;

public class ImageResizerImpl implements ImageResizer {

    private String path;

    public final void setPath(String path)
    {
        this.path = path;
    }

    public final String getPath()
    {
        return path;
    }

    public final String version() throws IOException, InterruptedException
    {
        String result = exec("identify -version");
        String part[] = result.trim().split("\r");
        return part[0].trim();
    }

    public final Size getSize( String filePath ) throws IOException, InterruptedException
    {
        verifyFileExists( filePath );

        String quotes = "\"";

        String result = exec( "identify -format \"%w,%h\" " + quotes + filePath + quotes );

        String dimension[] = result.trim().split(",");
        try {
            int width = Integer.parseInt(dimension[0], 10 );
            int height = Integer.parseInt(dimension[1], 10 );
            return new Size( width, height );

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public final void resize( String srcPath, String targetPath, Size targetSize, Rect cropRect )
            throws IOException, InterruptedException, ScaleException
    {
        String quotes = "\"";

        String resize = "";

        verifyFileExists( srcPath );

        File directoryPath = new File( new File( targetPath ).getParent() );

        if( ! directoryPath.exists() ) {
            if( ! directoryPath.mkdirs() ) {
                throw new IOException( "A filesystem directory could not be crated for " + targetPath );
            }
        }

        if( targetSize != null ) {

            int targetWidth = targetSize.getWidth();
            int targetHeight = targetSize.getHeight();

            if ( targetWidth > 0 && targetHeight > 0 ) {
                resize = " -resize " + targetWidth + 'x' + targetHeight + ' ';
            } else if ( targetHeight > 0) {
                resize = " -resize x" + targetHeight + ' ';
            } else if ( targetWidth > 0) {
                resize = " -resize " + targetWidth + ' ';
            }
        }

        String crop = "";

        if( cropRect!= null ) {

            int cropWidth = cropRect.getWidth();
            int cropHeight = cropRect.getHeight();
            int top = cropRect.getTop();
            int left = cropRect.getLeft();

            crop = " +repage -extract " +quotes + cropWidth + "x" + cropHeight + "+" + left + "+" + top + quotes + " ";
        }

        String command =
                "convert"+
                " "+
                crop +
                resize +
                // So things don't go wrong if you render CMYK sources...
                " -colorspace RGB " +
                quotes + srcPath + quotes + ' ' +
                quotes + targetPath + quotes;

        exec( command );
    }

    private String exec( String args) throws IOException, InterruptedException
    {
        String command = getPath() + "/" + args;

        Process process = null;

        try
        {
            process = Runtime.getRuntime().exec( command );
            process.waitFor();

            int c;
            StringWriter sw = new StringWriter();
            InputStream is = process.getInputStream();
            while ( ( c = is.read() ) != -1 )
            {
                sw.write( c );
            }

            return sw.toString();
        }
        finally
        {
            if ( process != null )
            {
                close( process.getErrorStream() );
                close( process.getInputStream() );
                close( process.getOutputStream() );
            }
            try
            {
                process.destroy();
            }
            catch (NullPointerException np)
            {

            }
        }
    }

    private void verifyFileExists( String filePath ) throws IOException
    {
        File file = new File( filePath );
        if( ! file.exists() ) {
            throw new IOException( "File missing: " + path );
        }
    }

    private void close( Closeable closeable )
    {
        if ( closeable != null )
        {
            if ( closeable instanceof Flushable )
            {
                try
                {
                    ( (Flushable) closeable ).flush();
                }
                catch (IOException e)
                {

                }
            }
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {

            }
        }
    }
}
