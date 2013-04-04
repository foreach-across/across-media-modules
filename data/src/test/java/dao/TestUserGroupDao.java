package dao;

import com.foreach.imageserver.business.taxonomy.UserGroup;
import com.foreach.imageserver.dao.UserGroupDao;
import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TestUserGroupDao extends AbstractDaoTest {

    @Autowired
    private UserGroupDao userGroupDao;

    @Test
    public void getUserGroupTest() {
        UserGroup userGroup = new UserGroup();
        userGroup.setGroupId(1);
        userGroup.setUserKey("admin");

        UserGroup fetched = userGroupDao.getUserGroup(userGroup);

        Assert.assertNotNull(fetched);

        Assert.assertEquals( userGroup.getGroupId(), fetched.getGroupId() );
        Assert.assertEquals( userGroup.getUserKey(), fetched.getUserKey() );
    }
}
