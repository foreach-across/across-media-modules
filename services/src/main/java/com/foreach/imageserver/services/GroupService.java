package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Group;

import java.util.List;

public interface GroupService
{
    Group getGroupById( int id );
	List<Group> allGroupsByApplicationId( int applicationId );
}
