package com.foreach.imageserver.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Arne Vandamme
 */
public class TestImageModificationDto
{
	private ImageModificationDto dto;

	@BeforeEach
	public void before() {
		dto = new ImageModificationDto();
	}

	@Test
	public void defaultModificationNotRegistered() {
		assertFalse( dto.isRegistered() );
		assertNull( dto.getBaseResolutionId() );
	}

	@Test
	public void registeredFalseIfBaseResolutionIdSetButNotMatchingOutputResolution() {
		dto.setBaseResolutionId( 3L );
		assertFalse( dto.isRegistered() );
		assertEquals( Long.valueOf( 3L ), dto.getBaseResolutionId() );

		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4L );
		dto.setResolution( res );
		assertFalse( dto.isRegistered() );
	}

	@Test
	public void registeredFalseIfBaseResolutionIdNotSet() {
		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4L );
		dto.setResolution( res );
		assertFalse( dto.isRegistered() );
	}

	@Test
	public void registeredTrueIfBaseResolutionIdMatchesOutputResolutionId() {
		ImageResolutionDto res = new ImageResolutionDto();
		res.setId( 4L );
		dto.setResolution( res );
		dto.setBaseResolutionId( 4L );

		assertTrue( dto.isRegistered() );
	}
}
