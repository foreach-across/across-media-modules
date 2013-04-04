package com.foreach.imageserver.services.authentication;

import com.foreach.imageserver.business.user.AppUser;
import com.foreach.imageserver.services.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.StringUtils;

public final class AuthenticationUserService implements UserDetailsService
{

    @Autowired
    private UserService userService;

    public UserDetails loadUserByUsername(String userName)
    {
	    if ( !StringUtils.hasText( userName )) {
		    return null;
	    }

        AppUser user = userService.getAppUserByName( userName );
	    if ( user == null) {
		    return null;
	    }

        AuthenticationUser authUser = new AuthenticationUser( user );
        return authUser;
    }
}
