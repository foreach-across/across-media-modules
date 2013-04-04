package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.taxonomy.UserGroup;
import org.springframework.stereotype.Repository;

@Repository
public interface UserGroupDao {

    UserGroup getUserGroup(UserGroup userGroup);
}
