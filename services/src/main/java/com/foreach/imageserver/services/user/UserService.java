package com.foreach.imageserver.services.user;

import com.foreach.imageserver.business.user.AppUser;

public interface UserService
{
    AppUser getAppUserByName( String userName );
}
