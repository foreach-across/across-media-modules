package com.foreach.imageserver.services.paths;

@Deprecated
public class ImageSpecifier {

    private long imageId;

    private int width; // these are optional, but if version is defined, one of width or height must also be defined
    private int height;
    private int version;

    private String fileType;

    public final long getImageId() {
        return imageId;
    }

    public final void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public final int getWidth() {
        return width;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final int getHeight() {
        return height;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final int getVersion() {
        return version;
    }

    public final void setVersion(int version) {
        this.version = version;
    }

    public final String getFileType() {
        return fileType;
    }

    public final void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public final String toString()
    {
        StringBuilder path = new StringBuilder();

        path.append( imageId );

        if( (width != 0) || (height != 0) ) {
            path.append( "_" );
            if( width != 0 ) {
                path.append( width );
            }
            path.append( "x" );
            if( height != 0 ) {
                path.append( height );
            }
            if( version != 0 ) {
                path.append( "_" );
                path.append( version );
            }
        }
        path.append( "." );
        path.append( fileType );

        return path.toString();
    }

    public final String baseImageName()
    {
        return new StringBuilder()
                .append( imageId )
                .append( "." )
                .append( fileType )
                .toString();
    }
}
