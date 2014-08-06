package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.Context;
import com.foreach.imageserver.core.business.ContextImageResolution;
import com.foreach.imageserver.core.business.ImageResolution;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageResolutionRepositoryTest extends AbstractIntegrationTest {

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
	    imageResolution.getTags().add( "one" );
	    imageResolution.getTags().add( "two" );
	    imageResolutionRepository.create( imageResolution );

        ImageResolution imageResolutionFromDb = imageResolutionRepository.getById(101010);
        assertNotNull(imageResolutionFromDb);
        assertEquals(101010, imageResolutionFromDb.getId());
        assertEquals(222, imageResolutionFromDb.getWidth());
        assertEquals(333, imageResolutionFromDb.getHeight());
	    assertEquals( 2, imageResolutionFromDb.getTags().size() );
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

	    imageResolutionRepository.create( createImageResolution( 10, 111, 222 ) );
	    imageResolutionRepository.create( createImageResolution( 11, 1111, 2222 ) );
	    imageResolutionRepository.create( createImageResolution( 12, 11111, 22222 ) );

	    imageResolutionRepository.create( createImageResolution( 14, 555, 666 ) );

        contextImageResolutionRepository.create( createContextImageResolution( -10, 10) );
	    contextImageResolutionRepository.create( createContextImageResolution( -10, 11) );
	    contextImageResolutionRepository.create( createContextImageResolution( -10, 12) );
        contextImageResolutionRepository.create( createContextImageResolution( -11, 14) );

        List<ImageResolution> imageResolutions10 = imageResolutionRepository.getForContext(-10);
        assertEquals( 3, imageResolutions10.size() );

        assertEquals( 10, imageResolutions10.get( 0 ).getId() );
        assertEquals(111, imageResolutions10.get(0).getWidth());
        assertEquals( 222, imageResolutions10.get( 0 ).getHeight() );

        assertEquals(11, imageResolutions10.get(1).getId());
	    assertEquals( 1111, imageResolutions10.get( 1 ).getWidth() );
        assertEquals(2222, imageResolutions10.get(1).getHeight());

        assertEquals( 12, imageResolutions10.get( 2 ).getId() );
        assertEquals(11111, imageResolutions10.get(2).getWidth());
	    assertEquals( 22222, imageResolutions10.get( 2 ).getHeight() );

	    List<ImageResolution> imageResolutions11 = imageResolutionRepository.getForContext(-11);
	    assertEquals( 1, imageResolutions11.size() );

	    Collection<Context> newContexts = new ArrayList<>();
	    newContexts.add( context11 );
	    imageResolutionRepository.updateContextsForResolution( 10, newContexts );

	    List<ImageResolution> imageResolutions11AfterUpdate = imageResolutionRepository.getForContext(-11);
	    assertEquals( 2, imageResolutions11AfterUpdate.size() );

	    List<ImageResolution> imageResolutions10AfterUpdate = imageResolutionRepository.getForContext(-10);
	    assertEquals( 2, imageResolutions10AfterUpdate.size() );
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
