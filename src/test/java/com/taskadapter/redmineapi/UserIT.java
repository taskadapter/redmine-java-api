package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.GroupFactory;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import org.apache.http.client.HttpClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

public class UserIT {
    private static final User OUR_USER = IntegrationTestHelper.getOurUser();

    private static UserManager userManager;

    private static Integer nonAdminUserId;
    private static String nonAdminUserLogin;
    private static String nonAdminPassword;

    @BeforeClass
    public static void oneTimeSetup() {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        userManager = mgr.getUserManager();
        try {
            createNonAdminUser();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void afterClass() {
        try {
            userManager.deleteUser(nonAdminUserId);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createNonAdminUser() throws RedmineException {
        User user = UserGenerator.generateRandomUser();
        User nonAdminUser = userManager.createUser(user);
        nonAdminUserId = nonAdminUser.getId();
        nonAdminUserLogin = user.getLogin();
        nonAdminPassword = user.getPassword();
    }

    @Test
    public void usersAreLoadedByAdmin() throws RedmineException {
        List<User> users = userManager.getUsers();
        assertThat(users).isNotEmpty();
    }

    @Test(expected = NotAuthorizedException.class)
    public void usersCannotBeLoadedByNotAdmin() throws RedmineException {
        getNonAdminManager().getUserManager().getUsers();
        fail("Must have failed with NotAuthorizedException.");
    }

    @Test
    public void userCanBeLoadedByIdByNonAdmin() throws RedmineException {
        User userById = getNonAdminManager().getUserManager().getUserById(nonAdminUserId);
        assertThat(userById.getId()).isEqualTo(nonAdminUserId);
    }

    @Test
    public void testGetCurrentUser() throws RedmineException {
        User currentUser = userManager.getCurrentUser();
        assertThat(currentUser.getId()).isEqualTo(OUR_USER.getId());
        assertThat(currentUser.getLogin()).isEqualTo(OUR_USER.getLogin());
    }

    @Test
    public void testGetUserById() throws RedmineException {
        User loadedUser = userManager.getUserById(OUR_USER.getId());
        assertThat(loadedUser.getId()).isEqualTo(OUR_USER.getId());
        assertThat(loadedUser.getLogin()).isEqualTo(OUR_USER.getLogin());
    }

    @Test
    public void userCanBeFoundByFreeFormSearch() throws RedmineException {
        final User user = UserFactory.create();
        user.setLogin("login" + System.currentTimeMillis());
        final String name = "UniqueName";
        user.setFirstName(name);
        user.setLastName("LName");
        user.setMail("aa@aaa.ccc");
        Integer id = null;
        try {
            final User created = userManager.createUser(user);
            id = created.getId();

            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            List<User> list = userManager.getUsers(params).getResults();
            assertThat(list.size()).isEqualTo(1);
            final User loaded = list.get(0);
            assertThat(loaded.getFirstName()).isEqualTo(name);
        } finally {
            userManager.deleteUser(id);
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNonExistingId() throws RedmineException {
        userManager.getUserById(999999);
    }

    @Test
    public void testCreateUser() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = UserGenerator.generateRandomUser();
            createdUser = userManager.createUser(userToCreate);

            assertThat(createdUser).isNotNull();

            assertThat(createdUser.getLogin()).isEqualTo(userToCreate.getLogin());
            assertThat(createdUser.getFirstName()).isEqualTo(userToCreate.getFirstName());
            assertThat(createdUser.getLastName()).isEqualTo(userToCreate.getLastName());
            assertThat(createdUser.getId()).isNotNull();
        } finally {
            if (createdUser != null) {
                userManager.deleteUser(createdUser.getId());
            }
        }
    }
    
    @Test
    public void testCreateUserWithAuthSource() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = UserGenerator.generateRandomUser();
            userToCreate.setAuthSourceId(1);
            createdUser = userManager.createUser(userToCreate);

            // Redmine doesn't return it, so let's consider a non-exceptional return as success for now.
            assertThat(createdUser).isNotNull();
        } finally {
            if (createdUser != null) {
                userManager.deleteUser(createdUser.getId());
            }
        }
    }

    @Test
    public void testUpdateUser() throws RedmineException {
        User userToCreate = UserFactory.create();
        userToCreate.setFirstName("fname2");
        userToCreate.setLastName("lname2");
        long randomNumber = new Date().getTime();
        userToCreate.setLogin("login33" + randomNumber);
        userToCreate.setMail("email" + randomNumber + "@somedomain.com");
        userToCreate.setPassword("1234asdf");
        User createdUser = userManager.createUser(userToCreate);
        Integer userId = createdUser.getId();
        try {
            String newFirstName = "fnameNEW";
            String newLastName = "lnameNEW";
            String newMail = "newmail" + randomNumber + "@asd.com";
            createdUser.setFirstName(newFirstName);
            createdUser.setLastName(newLastName);
            createdUser.setMail(newMail);

            userManager.update(createdUser);

            User updatedUser = userManager.getUserById(userId);

            assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
            assertThat(updatedUser.getMail()).isEqualTo(newMail);
            assertThat(updatedUser.getId()).isEqualTo(userId);
        } finally {
            userManager.deleteUser(userId);
        }
    }

    @Test
    public void userCanBeDeleted() throws RedmineException {
        User user = UserGenerator.generateRandomUser();
        User createdUser = userManager.createUser(user);
        Integer newUserId = createdUser.getId();

        try {
            userManager.deleteUser(newUserId);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        try {
            userManager.getUserById(newUserId);
            fail("Must have failed with NotFoundException because we tried to delete the user");
        } catch (NotFoundException e) {
            // ignore: the user should not be found
        }
    }

    @Test(expected = NotFoundException.class)
    public void deletingNonExistingUserThrowsNFE() throws RedmineException {
        userManager.deleteUser(999999);
    }

    /**
     * Requires Redmine 2.1+
     */
    @Test
    public void testAddUserToGroup() throws RedmineException {
        final Group template = GroupFactory.create("group " + System.currentTimeMillis());
        final Group group = userManager.createGroup(template);
        try {
            final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
            try {
                userManager.addUserToGroup(newUser, group);
                final Collection<Group> userGroups = userManager.getUserById(newUser.getId()).getGroups();
                assertThat(userGroups).hasSize(1);
                assertThat(userGroups.iterator().next().getName()).isEqualTo(group.getName());
            } finally {
                userManager.deleteUser(newUser.getId());
            }
        } finally {
            userManager.deleteGroup(group);
        }
    }


    /**
     * "add to group" operation used to be safe (idempotent) for Redmine 2.6.x, but FAILS for Redmine 3.0.0.
     * I submitted a bug: http://www.redmine.org/issues/19363, which was closed as "invalid".
     * Marking this test as "Ignored" for now.
     */
    @Ignore
    @Test
    public void addingUserToGroupTwiceDoesNotGiveErrors() throws RedmineException {
        final Group template = GroupFactory.create("some test " + System.currentTimeMillis());
        final Group group = userManager.createGroup(template);
        try {
            final User newUser = userManager.createUser(UserGenerator.generateRandomUser());
            try {
                userManager.addUserToGroup(newUser, group);
                userManager.addUserToGroup(newUser, group);
                final User userById = userManager.getUserById(newUser.getId());
                assertThat(userById.getGroups()).hasSize(1);
            } finally {
                userManager.deleteUser(newUser.getId());
            }
        } finally {
            userManager.deleteGroup(group);
        }
    }

    @Test
    public void testGroupCRUD() throws RedmineException {
        final Group template = GroupFactory.create("Template group " + System.currentTimeMillis());
        final Group created = userManager.createGroup(template);

        try {
            assertThat(created.getName()).isEqualTo(template.getName());
            final Group loaded = userManager.getGroupById(created.getId());
            assertThat(created.getName()).isEqualTo(loaded.getName());

            final Group update = GroupFactory.create(loaded.getId());
            update.setName("Group update " + System.currentTimeMillis());

            userManager.update(update);

            final Group loaded2 = userManager.getGroupById(created.getId());
            assertThat(loaded2.getName()).isEqualTo(update.getName());
        } finally {
            userManager.deleteGroup(created);
        }

        try {
            userManager.getGroupById(created.getId());
            fail("Group should be deleted but was found");
        } catch (NotFoundException e) {
            // OK!
        }
    }

    @Test
    public void testGetRoleById() throws RedmineException {
        final Collection<Role> roles = userManager.getRoles();
        for (Role r : roles) {
            final Role loaded = userManager.getRoleById(r.getId());
            assertThat(loaded.getName()).isEqualTo(r.getName());
            assertThat(loaded.getInherited()).isEqualTo(r.getInherited());
        }
    }

    @Test
    public void testRolesHasPermissions() throws RedmineException {
        final Collection<Role> roles = userManager.getRoles();
        for (Role r : roles) {
            final Role loaded = userManager.getRoleById(r.getId());
            if (loaded.getPermissions() != null && !loaded.getPermissions().isEmpty())
                return;

        }
        fail("Failed to find a role with a permissions");
    }

    @Test
    public void testGetRoles() throws RedmineException {
        assertThat(userManager.getRoles()).isNotEmpty();
    }

    @Test
    public void testUserDefaults() throws RedmineException {
        final User template = UserFactory.create();
        template.setFirstName("first name");
        template.setLastName("last name");
        final String email = System.currentTimeMillis() + "@globalhost.ru";
        template.setMail(email);
        template.setPassword("aslkdj32jnrfds7asdfn23()[]:kajsdf");
        final String login = "login" + System.currentTimeMillis();
        template.setLogin(login);
        final User result = userManager.createUser(template);
        try {
            Assert.assertNotNull(result.getId());
            Assert.assertEquals(login, result.getLogin());
            Assert.assertNull(result.getPassword());
            Assert.assertEquals("first name", result.getFirstName());
            Assert.assertEquals("last name", result.getLastName());
            Assert.assertEquals(email, result.getMail());
            Assert.assertNotNull(result.getCreatedOn());
            Assert.assertNull(result.getLastLoginOn());
            Assert.assertNotNull(result.getCustomFields());
        } finally {
            userManager.deleteUser(result.getId());
        }
    }

    @Test
    public void testLockUser() throws RedmineException {
        User user = userManager.getUserById(nonAdminUserId);
        user.setStatus(User.STATUS_LOCKED);
        userManager.update(user);

        user = userManager.getUserById(nonAdminUserId);
        Assert.assertEquals(User.STATUS_LOCKED, user.getStatus());

        user.setStatus(User.STATUS_ACTIVE);
        userManager.update(user);

        user = userManager.getUserById(nonAdminUserId);
        Assert.assertEquals(User.STATUS_ACTIVE, user.getStatus());
    }

    private RedmineManager getNonAdminManager() {
        final HttpClient httpClient = IntegrationTestHelper.getHttpClientForTestServer();
        return RedmineManagerFactory.createWithUserAuth(IntegrationTestHelper.getTestConfig().getURI(),
                    nonAdminUserLogin, nonAdminPassword, httpClient);
    }

}
