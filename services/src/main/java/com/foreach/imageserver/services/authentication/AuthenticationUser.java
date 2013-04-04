package com.foreach.imageserver.services.authentication;

import com.foreach.imageserver.business.user.AppUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

public final class AuthenticationUser implements UserDetails
{
	private static final long serialVersionUID = 1;
    private AppUser adminUser;

    public AuthenticationUser( AppUser adminUser )
    {
        this.adminUser = adminUser;
    }

    public Collection<GrantedAuthority> getAuthorities()
    {
	    GrantedAuthorityImpl authority = null;
	    if ( adminUser.isAdministrator()) {
			authority = new GrantedAuthorityImpl( "ROLE_ADMIN" );
	    } else {
	        authority = new GrantedAuthorityImpl( "ROLE_USER" );
	    }

        return Arrays.<GrantedAuthority>asList( authority );
    }

    public String getPassword()
    {
        return adminUser.getPassword();
    }

    public String getUsername()
    {
        return adminUser.getUsername();
    }

    public boolean isAccountNonExpired()
    {
        return true;
    }

    public boolean isAccountNonLocked()
    {
        return true;
    }

    public boolean isCredentialsNonExpired()
    {
        return true;
    }

    public boolean isEnabled()
    {
	    return adminUser.isActive();
    }
}
