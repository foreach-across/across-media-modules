package com.foreach.imageserver.services;

import com.foreach.imageserver.business.Dimensions;
import com.foreach.imageserver.business.ImageFile;
import com.foreach.imageserver.business.ImageModifier;
import com.foreach.imageserver.business.ImageType;
import com.foreach.imageserver.services.exceptions.ImageModificationException;
import com.foreach.imageserver.services.transformers.*;
import com.foreach.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
public abstract class TestImageModificationService<T extends ImageTransformerAction<Y>, Y>
{
	/**
	 * Test routing of ImageModifyAction.
	 */
	public static class TestImageModificationServiceWithImageModifyAction extends TestImageModificationService<ImageModifyAction, ImageFile>
	{
		@Override
		protected ActionTestItem<ImageModifyAction, ImageFile> createTestItem() {
			return new ActionTestItem<ImageModifyAction, ImageFile>()
			{
				private ImageFile original = new ImageFile( ImageType.JPEG, 0, null );
				private ImageFile modified = new ImageFile( ImageType.PNG, 10, null );
				private ImageModifier modifier = new ImageModifier();

				@Override
				public ImageModifyAction getAction() {
					return new ImageModifyAction( original, modifier );
				}

				@Override
				public ImageFile execute() {
					return modificationService.apply( original, modifier );
				}

				@Override
				public ImageFile getExpectedValue() {
					return modified;
				}
			};
		}
	}

	/**
	 * Test routing of ImageCalculateDimensionsAction.
	 */
	public static class TestImageModificationServiceWithImageCalculateDimensionsAction extends TestImageModificationService<ImageCalculateDimensionsAction, Dimensions>
	{
		@Override
		protected ActionTestItem<ImageCalculateDimensionsAction, Dimensions> createTestItem() {
			return new ActionTestItem<ImageCalculateDimensionsAction, Dimensions>()
			{
				private Dimensions result = new Dimensions( 333, 666 );
				private ImageFile original = new ImageFile( ImageType.JPEG, 0, null );

				@Override
				public ImageCalculateDimensionsAction getAction() {
					return new ImageCalculateDimensionsAction( original );
				}

				@Override
				public Dimensions execute() {
					return modificationService.calculateDimensions( original );
				}

				@Override
				public Dimensions getExpectedValue() {
					return result;
				}
			};
		}
	}

	@Autowired
	private ImageTransformer transformerOne;

	@Autowired
	private ImageTransformer transformerTwo;

	@Autowired
	private ImageTransformer transformerThree;

	@Autowired
	protected ImageModificationService modificationService;

	private ActionTestItem<T, Y> actionTestItem;

	public static interface ActionTestItem<T extends ImageTransformerAction<Y>, Y>
	{
		T getAction();

		Y execute();

		Y getExpectedValue();
	}

	@Before
	public void prepareForTest() throws Exception {
		actionTestItem = createTestItem();
	}

	protected abstract ActionTestItem<T, Y> createTestItem();

	@Test
	public void ifNoTransformersCanBeAppliedThenExceptionShouldBeThrown() {
		final ImageTransformerAction action = actionTestItem.getAction();
		when( transformerTwo.canExecute( action ) ).thenReturn( ImageTransformerPriority.UNABLE );

		boolean exceptionThrown = false;

		try {
			actionTestItem.execute();
		}
		catch ( ImageModificationException ime ) {
			exceptionThrown = true;
		}

		assertTrue( exceptionThrown );
		verify( transformerOne, times( 1 ) ).canExecute( action );
		verify( transformerTwo, times( 1 ) ).canExecute( action );
		verify( transformerThree, times( 1 ) ).canExecute( action );
	}

	@Test
	public void ifOnlyOneTransformerAvailableThatOneIsUsed() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocation ) throws Throwable {
				( (T) invocation.getArguments()[0] ).setResult( actionTestItem.getExpectedValue() );
				return null;
			}
		} ).when( transformerOne ).execute( action );

		Object applied = actionTestItem.execute();

		assertSame( actionTestItem.getExpectedValue(), applied );
		verify( transformerTwo, never() ).execute( action );
		verify( transformerThree, never() ).execute( action );
	}

	@Test
	public void ifMultipleTransformersThenFirstOneThatSucceedsIsReturned() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocation ) throws Throwable {
				( (T) invocation.getArguments()[0] ).setResult( actionTestItem.getExpectedValue() );
				return null;
			}
		} ).when( transformerThree ).execute( action );

		Object applied = actionTestItem.execute();

		assertSame( actionTestItem.getExpectedValue(), applied );
		verify( transformerOne, never() ).execute( action );
		verify( transformerTwo, never() ).execute( action );
	}

	@Test
	public void multipleTransformersAreTriedInOrder() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );

		doThrow( new RuntimeException() ).when( transformerTwo ).execute( action );

		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocation ) throws Throwable {
				( (T) invocation.getArguments()[0] ).setResult( actionTestItem.getExpectedValue() );
				return null;
			}
		} ).when( transformerOne ).execute( action );

		Object applied = actionTestItem.execute();

		assertSame( actionTestItem.getExpectedValue(), applied );
		verify( transformerOne, times( 1 ) ).execute( action );
		verify( transformerTwo, times( 1 ) ).execute( action );
		verify( transformerThree, times( 1 ) ).execute( action );
	}

	@Test
	public void preferredTransformersComeBeforeFallback() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canExecute( action ) ).thenReturn( ImageTransformerPriority.FALLBACK );

		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocation ) throws Throwable {
				( (T) invocation.getArguments()[0] ).setResult( actionTestItem.getExpectedValue() );
				return null;
			}
		} ).when( transformerOne ).execute( action );

		Object applied = actionTestItem.execute();

		assertSame( actionTestItem.getExpectedValue(), applied );
		verify( transformerTwo, times( 1 ) ).execute( action );
		verify( transformerThree, never() ).execute( action );
	}

	@Test
	public void preferredTransformerIsNotRequired() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.FALLBACK );
		when( transformerTwo.canExecute( action ) ).thenReturn( ImageTransformerPriority.FALLBACK );
		when( transformerThree.canExecute( action ) ).thenReturn( ImageTransformerPriority.FALLBACK );

		doThrow( new RuntimeException() ).when( transformerTwo ).execute( action );

		doAnswer( new Answer()
		{
			@Override
			public Object answer( InvocationOnMock invocation ) throws Throwable {
				( (T) invocation.getArguments()[0] ).setResult( actionTestItem.getExpectedValue() );
				return null;
			}
		} ).when( transformerOne ).execute( action );

		Object applied = actionTestItem.execute();

		assertSame( actionTestItem.getExpectedValue(), applied );
		verify( transformerOne, times( 1 ) ).execute( action );
		verify( transformerTwo, times( 1 ) ).execute( action );
		verify( transformerThree, times( 1 ) ).execute( action );
	}

	@Test
	public void ifAllTransformersFailThenExceptionShouldBeThrown() {
		ImageTransformerAction action = actionTestItem.getAction();

		when( transformerOne.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerTwo.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );
		when( transformerThree.canExecute( action ) ).thenReturn( ImageTransformerPriority.PREFERRED );

		doThrow( new RuntimeException() ).when( transformerTwo ).execute( action );
		doThrow( new RuntimeException() ).when( transformerOne ).execute( action );

		boolean exceptionThrown = false;

		try {
			actionTestItem.execute();
		}
		catch ( ImageModificationException ime ) {
			exceptionThrown = true;
		}

		assertTrue( exceptionThrown );
		verify( transformerOne, times( 1 ) ).execute( action );
		verify( transformerTwo, times( 1 ) ).execute( action );
		verify( transformerThree, times( 1 ) ).execute( action );
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
