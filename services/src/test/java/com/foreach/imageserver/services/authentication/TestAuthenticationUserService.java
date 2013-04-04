package com.foreach.imageserver.services.authentication;

import com.foreach.imageserver.business.user.AppUser;
import com.foreach.imageserver.services.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestAuthenticationUserService
{
	private AuthenticationUserService authenticationUserService;

	private UserService userService;

	@Before
	public void prepare()
	{
		authenticationUserService = new AuthenticationUserService();

		userService = mock( UserService.class );
		inject( authenticationUserService, "userService", userService );
	}

	@Test
	public void loadUserByEmptyUsername()
	{
		assertNull( authenticationUserService.loadUserByUsername( "" ) );
	}

	@Test
	public void loadMissingAdminUserByUsername()
	{
		String suppliedUsername = "root";

		when( userService.getAppUserByName( suppliedUsername )).thenReturn( null );

		assertNull( authenticationUserService.loadUserByUsername( suppliedUsername ) );
	}

	@Test
	public void loadUserByUsername()
	{
		String expectedPassword = "admin";

		String suppliedUsername = "root";

		AppUser expectedAdminUser = new AppUser();
		expectedAdminUser.setPassword( expectedPassword );

		when( userService.getAppUserByName( suppliedUsername ) ).thenReturn( expectedAdminUser );

		UserDetails retrievedUserDetails = authenticationUserService.loadUserByUsername( suppliedUsername );

		String retrievedPassword = retrievedUserDetails.getPassword();

		assertEquals( expectedPassword, retrievedPassword );
	}
}
