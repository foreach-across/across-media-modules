package com.foreach.imageserver.services;


import com.foreach.imageserver.business.taxonomy.UserGroup;
import com.foreach.imageserver.dao.UserGroupDao;
import com.foreach.shared.utils.InjectUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class TestUserGroup {

    private UserGroupService userGroupService;

    @Autowired
    private UserGroupDao userGroupDao;

    private UserGroup userGroup;

    @Before
    public void prepareForTest() {
        userGroupService = new UserGroupServiceImpl();

        userGroupDao = Mockito.mock(UserGroupDao.class);

        InjectUtils.inject(userGroupService, "userGroupDao", userGroupDao);
    }

    @Test
    public void getUserGroupTesr() {

        userGroup = new UserGroup();

        userGroupService.getUserGroup(userGroup);

        Mockito.verify(userGroupDao, Mockito.times(1)).getUserGroup(userGroup);
    }

}
