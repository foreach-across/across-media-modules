package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.GroupDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupServiceImpl implements GroupService
{
    @Autowired
    private GroupDao groupDao;

    public final Group getGroupById( int id )
    {
        return groupDao.getGroupById( id );
    }

	public final List<Group> allGroupsByApplicationId( int applicationId )
	{
		return groupDao.allGroupsByApplicationId( applicationId );
	}
}
