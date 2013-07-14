package com.foreach.imageserver.services;

import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import com.foreach.imageserver.services.transformers.ImageTransformer;
import com.foreach.imageserver.services.transformers.ImageTransformerPriority;
import com.foreach.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = { TestImageModificationService.TestConfig.class }, loader = MockedLoader.class)
public class TestImageModificationService
{
	@Autowired
	private ImageTransformer transformerOne;

	@Autowired
	private ImageTransformer transformerTwo;

	@Autowired
	private ImageTransformer transformerThree;

	@Autowired
	private ImageModificationService modificationService;

	private ImageFile original, modified;
	private ImageModifier modifier;

	@Before
	public void createTestInstances() {
		original = new ImageFile( ImageType.JPEG, 0, null );
		modified = new ImageFile( ImageType.PNG, 10, null );
		modifier = new ImageModifier();
	}

	@Test
	public void ifNoTransformersCanBeAppliedThenExceptionShouldBeThrown() {
		when( transformerTwo.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.UNABLE );

		boolean exceptionThrown = false;

		try {
			modificationService.apply( original, modifier );
		}
		catch ( ImageModificationException ime ) {
			exceptionThrown = true;
		}

		assertTrue( exceptionThrown );
		verify( transformerOne, times( 1 ) ).canApply( original, modifier );
		verify( transformerTwo, times( 1 ) ).canApply( original, modifier );
		verify( transformerThree, times( 1 ) ).canApply( original, modifier );
	}

	@Test
	public void ifOnlyOneTransformerAvailableThatOneIsUsed() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerOne.apply( original, modifier ) ).thenReturn( modified );

		ImageFile applied = modificationService.apply( original, modifier );

		assertSame( modified, applied );
		verify( transformerTwo, never() ).apply( original, modifier );
		verify( transformerThree, never() ).apply( original, modifier );
	}

	@Test
	public void IfMultipleTransformersThenFirstOneThatSucceedsIsReturned() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.apply( original, modifier ) ).thenReturn( modified );

		ImageFile applied = modificationService.apply( original, modifier );

		assertSame( modified, applied );
		verify( transformerOne, never() ).apply( original, modifier );
		verify( transformerTwo, never() ).apply( original, modifier );
	}

	@Test
	public void multipleTransformersAreTriedInOrder() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );

		when( transformerThree.apply( original, modifier ) ).thenReturn( null );
		when( transformerTwo.apply( original, modifier ) ).thenThrow( new RuntimeException() );
		when( transformerOne.apply( original, modifier ) ).thenReturn( modified );

		ImageFile applied = modificationService.apply( original, modifier );

		assertSame( modified, applied );
		verify( transformerOne, times( 1 ) ).apply( original, modifier );
		verify( transformerTwo, times( 1 ) ).apply( original, modifier );
		verify( transformerThree, times( 1 ) ).apply( original, modifier );
	}

	@Test
	public void preferredTransformersComesBeforeFallback() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.FALLBACK );

		when( transformerOne.apply( original, modifier ) ).thenReturn( modified );

		ImageFile applied = modificationService.apply( original, modifier );

		assertSame( modified, applied );
		verify( transformerTwo, times(1) ).apply( original, modifier );
		verify( transformerThree, never() ).apply( original, modifier );
	}

	@Test
	public void preferredTransformerIsNotRequired() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.FALLBACK );
		when( transformerTwo.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.FALLBACK );
		when( transformerThree.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.FALLBACK );

		when( transformerThree.apply( original, modifier ) ).thenReturn( null );
		when( transformerTwo.apply( original, modifier ) ).thenThrow( new RuntimeException() );
		when( transformerOne.apply( original, modifier ) ).thenReturn( modified );

		ImageFile applied = modificationService.apply( original, modifier );

		assertSame( modified, applied );
		verify( transformerOne, times( 1 ) ).apply( original, modifier );
		verify( transformerTwo, times( 1 ) ).apply( original, modifier );
		verify( transformerThree, times( 1 ) ).apply( original, modifier );
	}

	@Test
	public void ifAllTransformersFailThenExceptionShouldBeThrown() {
		when( transformerOne.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canApply( original, modifier ) ).thenReturn( ImageTransformerPriority.PREFERRED );

		when( transformerThree.apply( original, modifier ) ).thenReturn( null );
		when( transformerTwo.apply( original, modifier ) ).thenThrow( new RuntimeException() );
		when( transformerOne.apply( original, modifier ) ).thenThrow( new RuntimeException() );

		boolean exceptionThrown = false;

		try {
			modificationService.apply( original, modifier );
		}
		catch ( ImageModificationException ime ) {
			exceptionThrown = true;
		}

		assertTrue( exceptionThrown );
		verify( transformerOne, times( 1 ) ).apply( original, modifier );
		verify( transformerTwo, times( 1 ) ).apply( original, modifier );
		verify( transformerThree, times( 1 ) ).apply( original, modifier );
	}

	@Configuration
	public static class TestConfig
	{
		@Bean
		public ImageTransformer transformerOne() {
			return transformerMock( 1 );
		}

		@Bean
		public ImageTransformer transformerTwo() {
			return transformerMock( 2 );
		}

		@Bean
		public ImageTransformer transformerThree() {
			return transformerMock( 3 );
		}

		private ImageTransformer transformerMock( int priority ) {
			ImageTransformer t = mock( ImageTransformer.class );
			when( t.getPriority() ).thenReturn( priority );

			return t;
		}

		@Bean
		public ImageModificationService imageModificationService() {
			return new ImageModificationServiceImpl();
		}
	}
}
