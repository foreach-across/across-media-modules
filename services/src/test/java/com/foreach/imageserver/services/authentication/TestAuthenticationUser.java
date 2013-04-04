package com.foreach.imageserver.services.authentication;

import com.foreach.imageserver.business.user.AppUser;
import org.junit.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;

import java.util.Collection;

import static org.junit.Assert.*;

public class TestAuthenticationUser
{
	@Test
	public void getAuthoritiesForAdministrator()
	{
		AppUser suppliedAdminUser = new AppUser();
		suppliedAdminUser.setAdministrator( true );

		AuthenticationUser authenticationUser = new AuthenticationUser( suppliedAdminUser );

		Collection<GrantedAuthority> retrievedAuthorities = authenticationUser.getAuthorities();

		assertNotNull( retrievedAuthorities );
		assertTrue( retrievedAuthorities.size() == 1 );
		assertTrue( retrievedAuthorities.contains( new GrantedAuthorityImpl( "ROLE_ADMIN" ) ) );
	}

	@Test
	public void getAuthoritiesForUser()
	{
		AppUser suppliedAdminUser = new AppUser();
		suppliedAdminUser.setAdministrator( false );

		AuthenticationUser authenticationUser = new AuthenticationUser( suppliedAdminUser );

		Collection<GrantedAuthority> retrievedAuthorities = authenticationUser.getAuthorities();

		assertNotNull( retrievedAuthorities );
		assertTrue( retrievedAuthorities.size() == 1 );
		assertTrue( retrievedAuthorities.contains( new GrantedAuthorityImpl( "ROLE_USER" ) ) );
	}

	@Test
	public void getPassword()
	{
		String expectedPassword = "ThisIsNotSoSecret";

		AppUser suppliedAdminUser = new AppUser();
		suppliedAdminUser.setPassword( expectedPassword );

		AuthenticationUser authenticationUser = new AuthenticationUser( suppliedAdminUser );

		String retrievedPassword = authenticationUser.getPassword();

		assertEquals( expectedPassword, retrievedPassword );
	}

	@Test
	public void getUsername()
	{
		String expectedUsername = "root";

		AppUser suppliedAdminUser = new AppUser();
		suppliedAdminUser.setUsername( expectedUsername );

		AuthenticationUser authenticationUser = new AuthenticationUser( suppliedAdminUser );

		String retrievedUsername = authenticationUser.getUsername();

		assertEquals( expectedUsername, retrievedUsername );
	}

	@Test
	public void isAccountNonExpired()
	{
		assertTrue( ( new AuthenticationUser( new AppUser() ) ).isAccountNonExpired() );
	}

	@Test
	public void isAccountNonLocked()
	{
		assertTrue( ( new AuthenticationUser( new AppUser() ) ).isAccountNonLocked() );
	}

	@Test
	public void isCredentialsNonExpired()
	{
		assertTrue( ( new AuthenticationUser( new AppUser() ) ).isCredentialsNonExpired() );
	}

	@Test
	public void isEnabled()
	{
		AppUser suppliedAdminUser = new AppUser();
		suppliedAdminUser.setActive( true );

		AuthenticationUser authenticationUser = new AuthenticationUser( suppliedAdminUser );

		assertTrue( authenticationUser.isEnabled() );
	}
}


