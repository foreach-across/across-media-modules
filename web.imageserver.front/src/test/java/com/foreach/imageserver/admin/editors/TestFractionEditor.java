package com.foreach.imageserver.admin.editors;

import com.foreach.imageserver.business.math.Fraction;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestFractionEditor {

    private FractionEditor editor;

    @Before
    public void setup()
    {
        editor = new FractionEditor();
    }

    @Test
    public void consistency()
    {
        for( int i = -10; i<11; i++) {
            for( int j = 1; j<11; j++) {
                Fraction f = new Fraction( i, j );
                editor.setValue( f );
                assertEquals( f, editor.getValue() );
                assertEquals( f.getStringForUrl(), editor.getAsText() );

                editor.setAsText( f.toString() );
                assertEquals( f, editor.getValue() );
                assertEquals( f.getStringForUrl(), editor.getAsText() );
            }
        }
    }

    @Test
    public void emptyStringAndNull()
    {
        editor.setValue( null );
        assertEquals( null, editor.getValue() );
        assertEquals( "", editor.getAsText() );

        editor.setAsText( "" );
        assertEquals( null, editor.getValue() );
        assertEquals( "", editor.getAsText() );
    }
}
