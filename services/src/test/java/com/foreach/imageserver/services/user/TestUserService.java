package com.foreach.imageserver.services.user;

import com.foreach.imageserver.business.user.AppUser;
import com.foreach.imageserver.dao.UserDao;
import com.foreach.imageserver.services.AbstractServiceTest;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.foreach.shared.utils.InjectUtils.inject;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestUserService extends AbstractServiceTest {

    private List<AppUser> testUsers;

	private UserDao userDao;

	private UserService userService;

	@Before
	public void prepareForTest()
	{
		prepareListOfTestUsers();

		userService = new UserServiceImpl();

		userDao = mock( UserDao.class );
		inject( userService, "userDao", userDao );
	}

	@Test
	public void getAdminUserByUserName()
	{
		String userName = "Hashurbanipal";
		AppUser adminUser = getOneTestUser();

		when( userDao.getAppUserByName(userName) ).thenReturn( adminUser );

		AppUser retrievedUser = userService.getAppUserByName( userName );

		assertEquals( userName, retrievedUser.getUsername() );
		verify( userDao ).getAppUserByName( userName );
	}

	private AppUser getOneTestUser()
	{
		return testUsers.get( 2 );
	}

	private void prepareListOfTestUsers()
	{
		AppUser user1 = new AppUser();
		user1.setUsername( "Shalmaneser V" );

		AppUser user2 = new AppUser();
		user2.setUsername( "Esarhaddon" );

		AppUser user3 = new AppUser();
		user3.setUsername( "Hashurbanipal" );

		testUsers = new ArrayList<AppUser>();
		testUsers.add( user1 );
		testUsers.add( user2 );
		testUsers.add( user3 );
	}
}
