package com.foreach.imageserver.dao;

import com.foreach.imageserver.business.taxonomy.Group;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupDao
{
    Group getGroupById(int id);

	List<Group> allGroupsByApplicationId( int applicationId );

    void insertGroup( Group group );

    void updateGroup( Group group );

    void deleteGroup( int groupId );
}
