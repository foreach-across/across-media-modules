package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.UserGroup;

public interface UserGroupService {

    UserGroup getUserGroup( UserGroup userGroup );

	boolean isKeyForGroup( int groupId, String key );
}
