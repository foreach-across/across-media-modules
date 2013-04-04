package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.user.AppUser;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao
{
	AppUser getAppUserByName( String userName );
}

