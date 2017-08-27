package com.foreach.imageserver.core.rest.request;

import org.junit.Before;
import org.junit.Test;

import java.util.function.BooleanSupplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
public class TestViewImageRequest
{
	private ViewImageRequest request;

	@Before
	public void before() {
		request = new ViewImageRequest();
	}

	@Test
	public void noSecurityCallbackMeansInvalidCustomRequest() {
		assertFalse( request.isValidCustomRequest() );
	}

	@Test
	public void statusOfTheSecurityCallbackDeterminesValidity() {
		BooleanSupplier callback = mock( BooleanSupplier.class );
		request.setSecurityCheckCallback( callback );
		when( callback.getAsBoolean() ).thenReturn( true );
		assertTrue( request.isValidCustomRequest() );
	}

	@Test
	public void securityCallbackIsExecutedOnlyOnce() {
		BooleanSupplier callback = mock( BooleanSupplier.class );
		request.setSecurityCheckCallback( callback );
		assertFalse( request.isValidCustomRequest() );
		when( callback.getAsBoolean() ).thenReturn( true );
		assertFalse( request.isValidCustomRequest() );

		verify( callback, times( 1 ) ).getAsBoolean();
	}
}
