package dao;

import com.foreach.imageserver.business.user.AppUser;
import com.foreach.imageserver.dao.UserDao;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

public class TestUserDao extends AbstractDaoTest {

    @Autowired
    private UserDao userDao;

    @Test
    public void testAdminUserLogin() {
        String validUsername = "admin";
        String invalidUsername = "tstilgene";

        AppUser valid = userDao.getAppUserByName(validUsername);
        assertNotNull(valid);
        assertTrue(StringUtils.equals(valid.getUsername(), validUsername));

        AppUser invalid = userDao.getAppUserByName(invalidUsername);
        assertNull(invalid);
    }
}
