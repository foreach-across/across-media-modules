package com.foreach.imageserver.core.repositories;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.core.business.ImageContext;
import com.foreach.imageserver.core.business.ImageResolution;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ImageResolutionRepositoryTest extends AbstractIntegrationTest
{

	@Autowired
	private ImageResolutionRepository imageResolutionRepository;
	@Autowired
	private ImageContextRepository contextRepository;

	@Test
	public void getById() {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( 101010L );
		imageResolution.setWidth( 222 );
		imageResolution.setHeight( 333 );
		imageResolution.getTags().add( "one" );
		imageResolution.getTags().add( "two" );
		imageResolutionRepository.save( imageResolution );

		ImageResolution imageResolutionFromDb = imageResolutionRepository.findOne( 101010L );
		assertNotNull( imageResolutionFromDb );
		assertEquals( 101010L, imageResolutionFromDb.getId().longValue() );
		assertEquals( 222, imageResolutionFromDb.getWidth() );
		assertEquals( 333, imageResolutionFromDb.getHeight() );
		assertEquals( 2, imageResolutionFromDb.getTags().size() );
	}

	@Test
	public void getForContext() {

		ImageContext context10 = new ImageContext();
		context10.setId( -10L );
		context10.setCode( "application_code" );
		contextRepository.save( context10 );

		ImageContext context11 = new ImageContext();
		context11.setId( -11L );
		context11.setCode( "the_other_application_code" );
		contextRepository.save( context11 );

		imageResolutionRepository.save( createImageResolution( 10, 111, 222, Sets.newSet( context10 ) ) );
		imageResolutionRepository.save( createImageResolution( 11, 1111, 2222, Sets.newSet( context10 ) ) );
		imageResolutionRepository.save( createImageResolution( 12, 11111, 22222, Sets.newSet( context10 ) ) );
		imageResolutionRepository.save( createImageResolution( 14, 555, 666, Sets.newSet( context11 ) ) );

		List<ImageResolution> imageResolutions10 = imageResolutionRepository.getForContext( -10 );
		assertEquals( 3, imageResolutions10.size() );

		assertEquals( 10L, imageResolutions10.get( 0 ).getId().longValue() );
		assertEquals( 111, imageResolutions10.get( 0 ).getWidth() );
		assertEquals( 222, imageResolutions10.get( 0 ).getHeight() );

		assertEquals( 11L, imageResolutions10.get( 1 ).getId().longValue() );
		assertEquals( 1111, imageResolutions10.get( 1 ).getWidth() );
		assertEquals( 2222, imageResolutions10.get( 1 ).getHeight() );

		assertEquals( 12L, imageResolutions10.get( 2 ).getId().longValue() );
		assertEquals( 11111, imageResolutions10.get( 2 ).getWidth() );
		assertEquals( 22222, imageResolutions10.get( 2 ).getHeight() );

		List<ImageResolution> imageResolutions11 = imageResolutionRepository.getForContext( -11 );
		assertEquals( 1, imageResolutions11.size() );

		Collection<ImageContext> newContexts = new ArrayList<>();
		newContexts.add( context11 );
		imageResolutionRepository.updateContextsForResolution( 10, newContexts );

		List<ImageResolution> imageResolutions11AfterUpdate = imageResolutionRepository.getForContext( -11 );
		assertEquals( 2, imageResolutions11AfterUpdate.size() );

		List<ImageResolution> imageResolutions10AfterUpdate = imageResolutionRepository.getForContext( -10 );
		assertEquals( 2, imageResolutions10AfterUpdate.size() );
	}

	private ImageResolution createImageResolution( long id, int width, int height, Set<ImageContext> imageContexts ) {
		ImageResolution imageResolution = new ImageResolution();
		imageResolution.setId( id );
		imageResolution.setWidth( width );
		imageResolution.setHeight( height );
		imageResolution.setContexts( imageContexts );
		return imageResolution;
	}
}
