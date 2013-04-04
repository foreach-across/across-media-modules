package com.foreach.imageserver.admin.editors;

import com.foreach.imageserver.services.paths.ImageSpecifier;

import java.beans.PropertyEditorSupport;

public class ImageSpecifierEditor extends PropertyEditorSupport
{
    private ImageSpecifier imageFormat = null;

    @Override
    public final Object getValue()
    {
        return imageFormat;
    }

    @Override
    public final void setValue( Object object )
    {
        imageFormat = (ImageSpecifier) object;
    }

    public final String getAsText()
    {
        return ( imageFormat != null ) ? imageFormat.toString() : "";
    }

    public final void setAsText( String text) {

        String s = text;

        ImageSpecifier localFormat = new ImageSpecifier();

        int pos = s.lastIndexOf('.');
        if( pos != -1 ) {
            localFormat.setFileType(s.substring(pos + 1));
            s = s.substring( 0, pos );
        }


        pos = s.lastIndexOf('_');
        if ( ( pos != -1 ) && ( pos != s.indexOf('_') ) )
        {
            try {
                localFormat.setVersion( Integer.parseInt( s.substring( pos + 1 ), 10 ) );
            } catch ( NumberFormatException nf ) {

            }
            s = s.substring( 0, pos );
        }

        pos = s.lastIndexOf('x');
        if( pos != -1 ) {
            try {
                localFormat.setHeight(Integer.parseInt(s.substring(pos + 1), 10));
            } catch ( NumberFormatException nf ) {

            }
            s = s.substring( 0, pos );
        }

        pos = s.lastIndexOf('_');
        if( pos != -1 )
        {
            try {
                localFormat.setWidth(Integer.parseInt(s.substring(pos + 1), 10));
            } catch ( NumberFormatException nf ) {

            }
            s = s.substring( 0, pos );
        }

        try {
            localFormat.setImageId(Long.parseLong(s, 10));
        } catch ( NumberFormatException nf ) {

        }

        imageFormat = localFormat;
    }
}
