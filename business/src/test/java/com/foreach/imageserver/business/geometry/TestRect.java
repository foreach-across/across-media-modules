package com.foreach.imageserver.business.geometry;


import com.foreach.imageserver.business.math.Fraction;
import static org.junit.Assert.*;
import org.junit.Test;

public class TestRect {

    @Test
    public void coordinates()
    {
        int left = 100;
        int top = 10;
        int width = 800;
        int height = 205;

        Rect rect = new Rect( new Point( left, top ), new Size( width, height) );

        assertEquals( width, rect.getWidth() );
        assertEquals( height, rect.getHeight() );
        assertEquals( top, rect.getTop() );
        assertEquals( left, rect.getLeft() );
        assertEquals( left+width, rect.getRight() );
        assertEquals( top+height, rect.getBottom() );
    }

    @Test
    public void scale()
    {
        int left = 20;
        int top = 30;
        int width = 400;
        int height = 300;

        Rect rect = new Rect( new Point( left, top ), new Size( width, height) );
        Rect scaledRect = rect.scaleBy( new Fraction( 1, 10 ) );
        Rect expected = new Rect( new Point( left / 10, top / 10 ), new Size( width / 10, height / 10) );

        assertEquals( expected, scaledRect );

        scaledRect = rect.scaleBy( new Fraction( 3, 2 ) );
        expected = new Rect( new Point( 3 * left / 2, 3 * top / 2 ), new Size( 3 * width / 2, 3 * height / 2) );

        assertEquals( expected, scaledRect );

    }

	@Test
	public void pointContainment()
	{
		int left = 20;
		int top = 30;
		int width = 400;
		int height = 300;

		Rect rect = new Rect( new Point( left, top ), new Size( width, height) );

		assertEquals( true, rect.containsPoint( new Point( 20, 30) ) );
		assertEquals( false, rect.containsPoint( new Point( 420, 330) ) );
		assertEquals( false, rect.containsPoint( new Point( 420, 329) ) );
		assertEquals( false, rect.containsPoint( new Point( 419, 330) ) );
		assertEquals( true, rect.containsPoint( new Point( 419, 329) ) );
	}

	@Test
	public void rectContainment()
	{
		int left = 20;
		int top = 30;
		int width = 400;
		int height = 300;

		Rect rect = new Rect( new Point( left, top ), new Size( width, height) );

		assertEquals( true, rect.withinRect( rect ) );

		assertEquals( true, rect.withinRect(
				new Rect( new Point( left - 1, top ), new Size( width + 1, height) )
		) );
		assertEquals( true, rect.withinRect(
				new Rect( new Point( left, top - 1 ), new Size( width, height + 1) )
		) );
		assertEquals( true, rect.withinRect(
				new Rect( new Point( left - 1, top - 1 ), new Size( width + 1, height + 1) )
		) );

		assertEquals( false, rect.withinRect(
				new Rect( new Point( left + 1, top ), new Size( width - 1, height) )
		) );
		assertEquals( false, rect.withinRect(
				new Rect( new Point( left, top + 1 ), new Size( width, height - 1) )
		) );
		assertEquals( false, rect.withinRect(
				new Rect( new Point( left + 1, top + 1 ), new Size( width - 1, height - 1) )
		) );

	}

}
