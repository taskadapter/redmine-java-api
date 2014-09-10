package com.taskadapter.redmineapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taskadapter.redmineapi.bean.User;

public class UserIntegrationTest {
    private static final User OUR_USER = IntegrationTestHelper.getOurUser();

    private static RedmineManager mgr;
    private static String projectKey;
    private static Integer nonAdminUserId;
    private static String nonAdminUserLogin;
    private static String nonAdminPassword;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        try {
            projectKey = IntegrationTestHelper.createProject(mgr);
            createNonAdminUser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, projectKey);
    }

    private static void createNonAdminUser() throws RedmineException {
        User user = UserGenerator.generateRandomUser();
        User nonAdminUser = mgr.createUser(user);
        nonAdminUserId = nonAdminUser.getId();
        nonAdminUserLogin = user.getLogin();
        nonAdminPassword = user.getPassword();
    }

    @Test
    public void usersAreLoadedByAdmin() {
        try {
            List<User> users = mgr.getUsers();
            assertTrue(users.size() > 0);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test(expected = NotAuthorizedException.class)
    public void usersCannotBeLoadedByNotAdmin() throws RedmineException {
        getNonAdminManager().getUsers();
        fail("Must have failed with NotAuthorizedException.");
    }

    @Test
    public void userCanBeLoadedByIdByNonAdmin() throws RedmineException {
        User userById = getNonAdminManager().getUserById(nonAdminUserId);
        assertEquals(nonAdminUserId, userById.getId());
    }

    @Test
    public void testGetCurrentUser() throws RedmineException {
        User currentUser = mgr.getCurrentUser();
        assertEquals(OUR_USER.getId(), currentUser.getId());
        assertEquals(OUR_USER.getLogin(), currentUser.getLogin());
    }

    @Test
    public void testGetUserById() throws RedmineException {
        User loadedUser = mgr.getUserById(OUR_USER.getId());
        assertEquals(OUR_USER.getId(), loadedUser.getId());
        assertEquals(OUR_USER.getLogin(), loadedUser.getLogin());
        assertEquals(OUR_USER.getApiKey(), loadedUser.getApiKey());
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNonExistingId() throws RedmineException {
        mgr.getUserById(999999);
    }

    @Test
    public void testCreateUser() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = UserGenerator.generateRandomUser();
            createdUser = mgr.createUser(userToCreate);

            assertNotNull(
                    "checking that a non-null user is returned", createdUser);

            assertEquals(userToCreate.getLogin(), createdUser.getLogin());
            assertEquals(userToCreate.getFirstName(),
                    createdUser.getFirstName());
            assertEquals(userToCreate.getLastName(),
                    createdUser.getLastName());
            Integer id = createdUser.getId();
            assertNotNull(id);

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (createdUser != null) {
                mgr.deleteUser(createdUser.getId());
            }
        }
    }
    
    @Test
    public void testCreateUserWithAuthSource() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = UserGenerator.generateRandomUser();
            userToCreate.setAuthSourceId(1);
            createdUser = mgr.createUser(userToCreate);

            assertNotNull("checking that a non-null user is returned", createdUser);
            
//            Redmine doesn't return it, so let's consider a non-exceptional return as success for now. 
//            assertNotNull("checking that a non-null auth_source_id is returned", createdUser.getAuthSourceId());
//            assertEquals(1, createdUser.getAuthSourceId().intValue());

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            if (createdUser != null) {
                mgr.deleteUser(createdUser.getId());
            }
        }
    }

    @Test
    public void testUpdateUser() throws RedmineAuthenticationException,
            NotFoundException {
        User userToCreate = new User();
        userToCreate.setFirstName("fname2");
        userToCreate.setLastName("lname2");
        long randomNumber = new Date().getTime();
        userToCreate.setLogin("login33" + randomNumber);
        userToCreate.setMail("email" + randomNumber + "@somedomain.com");
        userToCreate.setPassword("1234asdf");
        try {
            User createdUser = mgr.createUser(userToCreate);
            Integer userId = createdUser.getId();
            assertNotNull(
                    "checking that a non-null project is returned", createdUser);

            String newFirstName = "fnameNEW";
            String newLastName = "lnameNEW";
            String newMail = "newmail" + randomNumber + "@asd.com";
            createdUser.setFirstName(newFirstName);
            createdUser.setLastName(newLastName);
            createdUser.setMail(newMail);

            mgr.update(createdUser);

            User updatedUser = mgr.getUserById(userId);

            assertEquals(newFirstName, updatedUser.getFirstName());
            assertEquals(newLastName, updatedUser.getLastName());
            assertEquals(newMail, updatedUser.getMail());
            assertEquals(userId, updatedUser.getId());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void userCanBeDeleted() throws RedmineException {
        User user = UserGenerator.generateRandomUser();
        User createdUser = mgr.createUser(user);
        Integer newUserId = createdUser.getId();

        try {
            mgr.deleteUser(newUserId);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
            mgr.getUserById(newUserId);
            fail("Must have failed with NotFoundException because we tried to delete the user");
        } catch (NotFoundException e) {
            // ignore: the user should not be found
        }
    }

    @Test(expected = NotFoundException.class)
    public void deletingNonExistingUserThrowsNFE() throws RedmineException {
        mgr.deleteUser(999999);
    }

    private RedmineManager getNonAdminManager() {
        RedmineManager nonAdminManager = RedmineManagerFactory.createWithUserAuth(IntegrationTestHelper.getTestConfig().getURI(),
                    nonAdminUserLogin, nonAdminPassword);
        return nonAdminManager;
    }

}
