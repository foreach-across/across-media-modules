package dao;

import com.foreach.imageserver.business.Application;
import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.ApplicationDao;
import com.foreach.imageserver.dao.GroupDao;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestGroupDao extends AbstractDaoTest
{
    @Autowired
    private GroupDao groupDao;

    @Autowired
    private ApplicationDao applicationDao;

    private Application app;

    @Before
    public void setup()
    {
        app = new Application();
        app.setName( "TestGroupDao.app" );
        applicationDao.insertApplication( app );
    }

    @After
    public void teardown()
    {
        applicationDao.deleteApplication( app.getId() );
    }


    @Test
    public void groupCrud()
    {
        Group group = createGroup();
        groupDao.insertGroup( group );
        assertTrue( group.getId() > 0);

        Group fetched = groupDao.getGroupById( group.getId() );
        compareGroups( group, fetched );

        group = modifyGroup( fetched );
        groupDao.updateGroup( group );
        fetched = groupDao.getGroupById( group.getId() );
        compareGroups( group, fetched );

        groupDao.deleteGroup( group.getId() );
    }

    private Group createGroup()
    {
        Group group = new Group();
        group.setApplicationId( app.getId() );
        group.setName( "TestGroupDao.group" );
        return group;
    }

    private void compareGroups( Group left, Group right )
    {
        assertEquals(left.getId(), right.getId());
        assertEquals( left.getName(), right.getName() );
        assertEquals( left.getApplicationId(), right.getApplicationId() );
    }

    private Group modifyGroup( Group group )
    {
        group.setName( "TestGroupDao.group.alter" );
        return group;
    }

	@Test(expected=DuplicateKeyException.class)
	public void duplicateName()
	{
		Group group = new Group();

		try {
			group.setApplicationId( app.getId() );
			group.setName( "TestGroupDao.group.duplicateName" );
			groupDao.insertGroup( group );

			// try to insert it again
			Group group2 = new Group();
			group2.setApplicationId( app.getId() );
			group2.setName( "TestGroupDao.group.duplicateName" );
			groupDao.insertGroup( group2 );
		} finally {
			groupDao.deleteGroup( group.getId() );
		}
	}

	@Test
    public void allGroupsByApplicationId()
    {
        List<Group> groups = groupDao.allGroupsByApplicationId(1);

        assertTrue( groups.size() > 0 );
    }
}
