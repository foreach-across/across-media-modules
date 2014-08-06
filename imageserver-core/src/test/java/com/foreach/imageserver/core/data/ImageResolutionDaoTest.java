package com.foreach.imageserver.core.data;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ContextImageResolution;
import com.foreach.imageserver.core.business.ImageResolution;
import com.foreach.imageserver.core.repositories.ContextImageResolutionRepository;
import com.foreach.imageserver.core.repositories.ContextRepository;
import com.foreach.imageserver.core.repositories.ImageResolutionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class ImageResolutionDaoTest extends AbstractIntegrationTest {

    @Autowired
    private ImageResolutionRepository imageResolutionRepository;
	@Autowired
	private ContextRepository contextRepository;
	@Autowired
	private ContextImageResolutionRepository contextImageResolutionRepository;

    @Test
    public void getById() {
	    ImageResolution imageResolution = new ImageResolution();
	    imageResolution.setId( 101010 );
	    imageResolution.setWidth( 222 );
	    imageResolution.setHeight( 333 );
	    imageResolutionRepository.create( imageResolution );

        ImageResolution imageResolutionFromDb = imageResolutionRepository.getById(101010);
        assertNotNull(imageResolutionFromDb);
        assertEquals(101010, imageResolutionFromDb.getId());
        assertEquals(222, imageResolutionFromDb.getWidth());
        assertEquals(333, imageResolutionFromDb.getHeight());
    }

    @Test
    public void getForContext() {

	    Context context10 = new Context();
	    context10.setId( -10 );
	    context10.setCode( "application_code" );
	    contextRepository.create( context10 );

	    Context context11 = new Context();
	    context11.setId( -11 );
	    context11.setCode( "the_other_application_code" );
	    contextRepository.create( context11 );

	    imageResolutionRepository.create( createImageResolution( 10, 1111, 222 ) );
	    imageResolutionRepository.create( createImageResolution( 11, 1111, 2222 ) );
	    imageResolutionRepository.create( createImageResolution( 12, 11111, 22222 ) );

	    imageResolutionRepository.create( createImageResolution( 14, 555, 666 ) );

        contextImageResolutionRepository.create( createContextImageResolution(  -10, 10) );
	    contextImageResolutionRepository.create( createContextImageResolution(  -10, 11) );
	    contextImageResolutionRepository.create( createContextImageResolution(  -10, 12) );
        contextImageResolutionRepository.create( createContextImageResolution(  -11, 14) );

        List<ImageResolution> imageResolutions = imageResolutionRepository.getForContext(-10);
        assertEquals(3, imageResolutions.size());

        assertEquals(10, imageResolutions.get(0).getId());
        assertEquals(111, imageResolutions.get(0).getWidth());
        assertEquals(222, imageResolutions.get(0).getHeight());

        assertEquals(11, imageResolutions.get(1).getId());
        assertNull(imageResolutions.get(1).getWidth());
        assertEquals(333, imageResolutions.get(1).getHeight());

        assertEquals(12, imageResolutions.get(2).getId());
        assertEquals(444, imageResolutions.get(2).getWidth());
        assertNull(imageResolutions.get(2).getHeight());
    }

	private ContextImageResolution createContextImageResolution( long contextId, long imageResolutionId ) {
		ContextImageResolution contextImageResolution = new ContextImageResolution();
		contextImageResolution.setContextId( contextId );
		contextImageResolution.setImageResolutionId( imageResolutionId );
		return contextImageResolution;
	}

	private ImageResolution createImageResolution( long id, int width, int height ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		return imageResolution;
	}
}
