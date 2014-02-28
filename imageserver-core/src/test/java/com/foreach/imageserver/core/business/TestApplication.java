package com.foreach.imageserver.core.business;

import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

public class TestApplication
{
	private final Random RANDOM = new Random( System.currentTimeMillis() );

	@Test
	public void equalsOnId() {
		Application left = new Application();
		left.setId( RANDOM.nextInt() );
		left.setName( "a" );

		Application right = new Application();
		right.setId( left.getId() + 1 );
		right.setName( "b" );

		assertFalse( left.equals( right ) );
		assertFalse( right.equals( left ) );

		right.setId( left.getId() );
		assertEquals( left, right );
		assertEquals( right, left );
		assertEquals( left.hashCode(), right.hashCode() );
	}

	@Test
	public void canBeManagedIfActiveAndCodeEquals() {
		Application application = new Application();
		application.setActive( true );
		application.setCode( UUID.randomUUID().toString() );

		assertTrue( application.canBeManaged( application.getCode() ) );
	}

	@Test
	public void canNeverBeManagedIfNoCodeSet() {
		Application application = new Application();
		application.setActive( true );

		assertFalse( application.canBeManaged( null ) );
		assertFalse( application.canBeManaged( UUID.randomUUID().toString() ) );
	}

	@Test
	public void canNeverBeManagedIfNotActive() {
		Application application = new Application();
		application.setActive( false );
		application.setCode( UUID.randomUUID().toString() );

		assertFalse( application.canBeManaged( application.getCode() ) );
	}
}
