package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport;
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
    private static User OUR_USER;

    private static UserManager userManager;

    private static Integer nonAdminUserId;
    private static User nonAdminUser;
    private static String nonAdminUserLogin;
    private static String nonAdminPassword;
    private static Transport transport;

    @BeforeClass
    public static void oneTimeSetup() {
        RedmineManager mgr = IntegrationTestHelper.createRedmineManager();
        transport = mgr.getTransport();
        userManager = mgr.getUserManager();
        OUR_USER = IntegrationTestHelper.getOurUser(transport);
        try {
            User userToCreate = UserGenerator.generateRandomUser(transport);
            nonAdminUser = userToCreate.create();
            nonAdminUserId = nonAdminUser.getId();
            nonAdminUserLogin = userToCreate.getLogin();
            // note that created users do NOT have passwords set due to security considerations
            nonAdminPassword = userToCreate.getPassword();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void afterClass() {
        try {
            nonAdminUser.delete();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
        String name = "UniqueName";
        User user = new User(transport)
                .setLogin("login" + System.currentTimeMillis())
                .setFirstName(name)
                .setLastName("LName")
                .setMail("aa@aaa.ccc");
        User created = user.create();
        try {
            Map<String, String> params = new HashMap<>();
            params.put("name", name);
            List<User> list = userManager.getUsers(params).getResults();
            assertThat(list.size()).isEqualTo(1);
            final User loaded = list.get(0);
            assertThat(loaded.getFirstName()).isEqualTo(name);
        } finally {
            created.delete();
        }
    }

    @Test(expected = NotFoundException.class)
    public void testGetUserNonExistingId() throws RedmineException {
        userManager.getUserById(999999);
    }

    @Test
    public void testCreateUser() throws RedmineException {
        User user = UserGenerator.generateRandomUser(transport);
        User createdUser = user.create();

        assertThat(createdUser).isNotNull();

        assertThat(createdUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(createdUser.getFirstName()).isEqualTo(user.getFirstName());
        assertThat(createdUser.getLastName()).isEqualTo(user.getLastName());
        assertThat(createdUser.getId()).isNotNull();

        createdUser.delete();
    }

    @Test
    public void testCreateUserWithAuthSource() throws RedmineException {
        User createdUser = null;
        try {
            User userToCreate = UserGenerator.generateRandomUser(transport);
            userToCreate.setAuthSourceId(1);
            createdUser = userToCreate.create();

            // Redmine doesn't return it, so let's consider a non-exceptional return as success for now.
            assertThat(createdUser).isNotNull();
        } finally {
            if (createdUser != null) {
                createdUser.delete();
            }
        }
    }

    @Test
    public void testUpdateUser() throws RedmineException {
        long randomNumber = new Date().getTime();

        String newFirstName = "fnameNEW";
        String newLastName = "lnameNEW";
        String newMail = "newmail" + randomNumber + "@asd.com";

        User user = new User(transport)
                .setFirstName("fname2")
                .setLastName("lname2")
                .setLogin("login33" + randomNumber)
                .setMail("email" + randomNumber + "@somedomain.com")
                .setPassword("1234asdf")
                .create();

        user.setFirstName(newFirstName)
                .setLastName(newLastName)
                .setMail(newMail)
                .update();
        try {
            User updatedUser = userManager.getUserById(user.getId());

            assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
            assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
            assertThat(updatedUser.getMail()).isEqualTo(newMail);
            assertThat(updatedUser.getId()).isEqualTo(user.getId());
        } finally {
            user.delete();
        }
    }

    @Test
    public void userCanBeDeleted() throws RedmineException {
        User user = UserGenerator.generateRandomUser(transport);
        User createdUser = user.create();
        Integer newUserId = createdUser.getId();

        try {
            createdUser.delete();
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
        new User(transport).setId(999999).delete();
    }

    /**
     * Requires Redmine 2.1+
     */
    @Test
    public void testAddUserToGroup() throws RedmineException {
        Group group = new Group(transport).setName("group " + System.currentTimeMillis())
                .create();
        try {
            User newUser = UserGenerator.generateRandomUser(transport).create();
            try {
                newUser.addToGroup(group.getId());
                final Collection<Group> userGroups = userManager.getUserById(newUser.getId()).getGroups();
                assertThat(userGroups).hasSize(1);
                assertThat(userGroups.iterator().next().getName()).isEqualTo(group.getName());
            } finally {
                newUser.delete();
            }
        } finally {
            group.delete();
        }
    }

    @Test
    public void testGroupCRUD() throws RedmineException {
        Group toCreate = new Group(transport).setName("Template group " + System.currentTimeMillis());
        Group created = toCreate.create();
        try {
            assertThat(created.getName()).isEqualTo(toCreate.getName());
            Group loaded = userManager.getGroupById(created.getId());
            assertThat(created.getName()).isEqualTo(loaded.getName());

            Group toUpdate = new Group(transport).setId(loaded.getId())
                    .setName("Group update " + System.currentTimeMillis());
            toUpdate.update();

            final Group loaded2 = userManager.getGroupById(created.getId());
            assertThat(loaded2.getName()).isEqualTo(toUpdate.getName());
        } finally {
            created.delete();
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
        String email = System.currentTimeMillis() + "@globalhost.ru";
        String login = "login" + System.currentTimeMillis();

        User user = new User(transport)
                .setFirstName("first name")
                .setLastName("last name")
                .setMail(email)
                .setPassword("aslkdj32jnrfds7asdfn23()[]:kajsdf")
                .setLogin(login)
                .create();

        try {
            Assert.assertNotNull(user.getId());
            Assert.assertEquals(login, user.getLogin());
            Assert.assertNull(user.getPassword());
            Assert.assertEquals("first name", user.getFirstName());
            Assert.assertEquals("last name", user.getLastName());
            Assert.assertEquals(email, user.getMail());
            Assert.assertNotNull(user.getCreatedOn());
            Assert.assertNull(user.getLastLoginOn());
            Assert.assertNotNull(user.getCustomFields());
        } finally {
            user.delete();
        }
    }

    @Test
    public void testLockUser() throws RedmineException {
        User user = userManager.getUserById(nonAdminUserId)
                .setStatus(User.STATUS_LOCKED);
        user.update();

        User loadedById = userManager.getUserById(nonAdminUserId);
        Assert.assertEquals(User.STATUS_LOCKED, loadedById.getStatus());

        loadedById.setStatus(User.STATUS_ACTIVE)
                .update();

        User loadedAgain = userManager.getUserById(nonAdminUserId);
        Assert.assertEquals(User.STATUS_ACTIVE, loadedAgain.getStatus());
    }

    private RedmineManager getNonAdminManager() {
        final HttpClient httpClient = IntegrationTestHelper.getHttpClientForTestServer();
        return RedmineManagerFactory.createWithUserAuth(IntegrationTestHelper.getTestConfig().getURI(),
                    nonAdminUserLogin, nonAdminPassword, httpClient);
    }

}
