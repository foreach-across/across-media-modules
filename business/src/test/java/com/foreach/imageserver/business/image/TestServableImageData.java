package com.foreach.imageserver.business.image;

import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class TestServableImageData {

    private ServableImageData image;

    private List<Integer> versions = Arrays.asList( 4, 0, 2, 3);

    @Before
    public void setup()
    {
        image = new ServableImageData();

        Set<Crop> crops = new HashSet<Crop>();
        for( Integer version : versions ) {
            crops.add( createCrop( version ) );
        }
        image.setCrops( crops );
    }

    private Crop createCrop( int version )
    {
        Crop crop = new Crop();
        crop.setVersion( version );
        return crop;
    }

    @Test
    public void versionList()
    {
        List<Integer> expected = new ArrayList<Integer>();
        expected.addAll( versions );
        Collections.sort( expected );

        assertEquals( expected, image.getVersionList() );
    }
}
