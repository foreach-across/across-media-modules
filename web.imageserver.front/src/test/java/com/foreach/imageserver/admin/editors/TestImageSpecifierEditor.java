package com.foreach.imageserver.admin.editors;

import com.foreach.imageserver.services.paths.ImageSpecifier;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestImageSpecifierEditor {

    private ImageSpecifierEditor editor;

    @Before
    public void setup()
    {
        editor = new ImageSpecifierEditor();
    }

    @Test
    public void basic()
    {
        String formatString = "1776.qnx";

        editor.setAsText( formatString );

        ImageSpecifier format = (ImageSpecifier) editor.getValue();

        assertEquals( 1776, format.getImageId() );
        assertEquals( 0, format.getWidth() );
        assertEquals( 0, format.getHeight() );
        assertEquals( 0, format.getVersion() );
        assertEquals( "qnx", format.getFileType() );

        assertEquals( formatString, format.toString() );
    }

    @Test
    public void width()
    {
        String formatString = "1776_400x.qnx";

        editor.setAsText( formatString );

        ImageSpecifier format = (ImageSpecifier) editor.getValue();

        assertEquals( 1776, format.getImageId() );
        assertEquals( 400, format.getWidth() );
        assertEquals( 0, format.getHeight() );
        assertEquals( 0, format.getVersion() );
        assertEquals( "qnx", format.getFileType() );

        assertEquals( formatString, format.toString() );
    }

    @Test
    public void widthAndHeight()
    {
        String formatString = "1776_400x317.qnx";

        editor.setAsText( formatString );

        ImageSpecifier format = (ImageSpecifier) editor.getValue();

        assertEquals( 1776, format.getImageId() );
        assertEquals( 400, format.getWidth() );
        assertEquals( 317, format.getHeight() );
        assertEquals( 0, format.getVersion() );
        assertEquals( "qnx", format.getFileType() );

        assertEquals( formatString, format.toString() );
    }

    @Test
    public void height()
    {
        String formatString = "1776_x317.qnx";

        editor.setAsText( formatString );

        ImageSpecifier format = (ImageSpecifier) editor.getValue();

        assertEquals( 1776, format.getImageId() );
        assertEquals( 0, format.getWidth() );
        assertEquals( 317, format.getHeight() );
        assertEquals( 0, format.getVersion() );
        assertEquals( "qnx", format.getFileType() );

        assertEquals( formatString, format.toString() );
    }


    @Test
    public void complex()
    {
        String formatString = "1776_400x317_2.qnx";

        editor.setAsText( formatString );

        ImageSpecifier format = (ImageSpecifier) editor.getValue();

        assertEquals( 1776, format.getImageId() );
        assertEquals( 400, format.getWidth() );
        assertEquals( 317, format.getHeight() );
        assertEquals( 2, format.getVersion() );
        assertEquals( "qnx", format.getFileType() );

        assertEquals( formatString, format.toString() );
    }
}
