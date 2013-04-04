package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.UserGroup;
import com.foreach.imageserver.dao.UserGroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserGroupServiceImpl implements UserGroupService {

    @Autowired
    private UserGroupDao userGroupDao;

    public final UserGroup getUserGroup(UserGroup userGroup) {
        return userGroupDao.getUserGroup(userGroup);
    }

	public final boolean isKeyForGroup( int groupId, String key )
	{
		UserGroup userGroup = new UserGroup();
		userGroup.setGroupId( groupId );
		userGroup.setUserKey( key );

		return ( getUserGroup( userGroup ) != null );
	}
}
