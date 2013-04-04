package com.foreach.imageserver.services.user;

import com.foreach.imageserver.business.user.AppUser;
import com.foreach.imageserver.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserDao userDao;

    public final AppUser getAppUserByName( String userName )
    {
        return userDao.getAppUserByName( userName );
    }
}
