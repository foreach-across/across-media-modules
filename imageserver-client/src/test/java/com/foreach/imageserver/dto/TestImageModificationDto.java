package com.foreach.imageserver.dto;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestImageModificationDto
{
	private ImageModificationDto dto;

	@Before
	public void before() {
		dto = new ImageModificationDto();
	}

	@Test
	public void defaultModificationNotRegistered() {
		assertFalse( dto.isRegistered() );
		assertEquals( Optional.empty(), dto.getBaseResolutionId() );
	}

	@Test
	public void registeredFalseIfBaseResolutionIdSetButNotMatchingOutputResolution() {
		dto.setBaseResolutionId( 3L );
		assertFalse( dto.isRegistered() );
		assertEquals( Optional.of( 3L ), dto.getBaseResolutionId() );

		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4 );
		dto.setResolution( res );
		assertFalse( dto.isRegistered() );
	}

	@Test
	public void registeredFalseIfBaseResolutionIdNotSet() {
		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4 );
		dto.setResolution( res );
		assertFalse( dto.isRegistered());
	}

	@Test
	public void registeredTrueIfBaseResolutionIdMatchesOutputResolutionId() {
		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4 );
		dto.setResolution( res );
		dto.setBaseResolutionId( 4L );

		assertTrue( dto.isRegistered() );
	}
}
