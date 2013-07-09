package com.foreach.imageserver.services;

import com.foreach.imageserver.business.taxonomy.Group;
import com.foreach.imageserver.dao.GroupDao;
import com.foreach.shared.utils.InjectUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

@Ignore
public class TestGroupService extends AbstractServiceTest
{
    private GroupService groupService;
    private GroupDao groupDao;

    private Group testGroup;
	
    private List<Group> testGroups;

    private int testId = 1;
    private String testName = "test";

    @Before
    public void prepareForTest()
    {
        groupService = new GroupServiceImpl();

        groupDao = Mockito.mock(GroupDao.class);

        InjectUtils.inject(groupService, "groupDao", groupDao);

        testGroup = new Group();
        testGroup.setId(testId);
        testGroup.setName(testName);

        Mockito.when(groupDao.getGroupById(testId)).thenReturn( testGroup );
	    
	    testGroup = new Group();
        testGroup.setId(testId);
        testGroup.setName(testName);
        
        Mockito.when(groupDao.getGroupById(testId)).thenReturn( testGroup );
        
        testGroups = new ArrayList<Group>();
        testGroups.add(testGroup);

        Mockito.when(groupDao.allGroupsByApplicationId(testId)).thenReturn( testGroups );
    }

    @Test
    public void testGetGroupById()
    {
        Group group = groupService.getGroupById( testId );

        Mockito.verify(groupDao, Mockito.times(1)).getGroupById(testId);

        Assert.assertNotNull(group);
        Assert.assertEquals(testId, group.getId());
        Assert.assertEquals(testName, group.getName());
    }

	@Test
    public void allGroupsByApplicationId()
    {
        List<Group> Groups = groupService.allGroupsByApplicationId(testId);

        Mockito.verify(groupDao, Mockito.times(1)).allGroupsByApplicationId( testId );

        Assert.assertNotNull(Groups);
        Assert.assertTrue(Groups.size() > 0);
        Assert.assertEquals(testGroups.size(), Groups.size());
    }
}
