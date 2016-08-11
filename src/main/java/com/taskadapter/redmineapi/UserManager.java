package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.DirectObjectsSearcher;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;
import java.util.Map;

/**
 * Works with Users and Groups.
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager redmineManager = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 UserManager userManager = redmineManager.getUserManager();
 * </pre>
 * <strong>Note that some operations with users require Redmine Admin privileges.</strong>
 *
 * <p>Sample usage:
 * <pre>
     users = mgr.getUserManager().getUsers();
 * </pre>
 *
 * @see RedmineManager#getUserManager()
 */
public class UserManager {
    private final Transport transport;

    UserManager(Transport transport) {
        this.transport = transport;
    }

    /**
     * @return the current user logged into Redmine
     */
    public User getCurrentUser() throws RedmineException {
        return transport.getCurrentUser();
    }

    public User createUser(User user) throws RedmineException {
        return transport.addObject(user);
    }

    /**
     * @param userId user identifier (numeric ID)
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException       if the user with the given id is not found
     */
    public void deleteUser(Integer userId) throws RedmineException {
        transport.deleteObject(User.class, Integer.toString(userId));
    }

    /**
     * Adds the given user to the given group.
     * <p>
     * Note: "add to group" operation used to be safe (idempotent) for Redmine 2.6.x, but FAILS for Redmine 3.0.0 when
     * executed twice on the same user. I submitted a bug: http://www.redmine.org/issues/19363
     *
     * @param user  - The user being added.
     * @param group - The new group of the user.
     * @throws RedmineException
     * @since Redmine 2.1
     */
    public void addUserToGroup(User user, Group group) throws RedmineException {
        transport.addUserToGroup(user.getId(), group.getId());
    }

    /**
     * Load list of users from the server.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     *
     * @return list of User objects
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<User> getUsers() throws RedmineException {
        return transport.getObjectsList(User.class, new BasicNameValuePair(
                "include", "memberships,groups"));
    }

    /**
     * <p>This method does NOT handle paging for you. You need to provide "offset" and "limit" parameters
     * if you want to control paging.
     * <p>Sample usage:
     <pre>
     final Map<String, String> params = new HashMap<String, String>();
     params.put("name", name);
     final List<User> users = userManager.getUsers(params);
     </pre>
     *
     * @param parameters http parameters: key/value pairs to append to the rest api request
     * @return resultsWrapper with raw response from Redmine REST API
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws RedmineException
     */
    public ResultsWrapper<User> getUsers(Map<String, String> parameters) throws RedmineException {
        return DirectObjectsSearcher.getObjectsListNoPaging(transport, parameters, User.class);
    }

    /**
     * This does NOT require Admin privileges by default Redmine installation (tested with Redmine 2.0.3).
     */
    public User getUserById(Integer userId) throws RedmineException {
        return transport.getObject(User.class, userId, new BasicNameValuePair(
                "include", "memberships,groups"));
    }

    /**
     * Load list of groups on the server.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     *
     * @return list of User objects
     * @throws RedmineAuthenticationException invalid or no API access key is used with the server, which
     *                                 requires authorization. Check the constructor arguments.
     * @throws NotFoundException
     * @throws RedmineException
     */
    public List<Group> getGroups() throws RedmineException {
        return transport.getObjectsList(Group.class);
    }

    /**
     * Returns the group based on its id.
     * <p>
     * <strong>This operation requires "Redmine Administrators" permission.</strong>
     *
     * @param id id of the group
     * @return the group
     * @throws RedmineException
     */
    public Group getGroupById(int id) throws RedmineException {
        return transport.getObject(Group.class, id);
    }

    /**
     * Returns the group based on its name.
     * <p>
     * <strong>This operation requires "Redmine Administrators" permission.</strong>
     *
     * @param name
     *            the name of the group
     * @return the group
     * @throws RedmineException
     */
    public Group getGroupByName(String name) throws RedmineException {
        return transport.getObject(Group.class, name);
    }

    /**
     * Creates a new group.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     * @return created group.
     * @throws RedmineException
     */
    public Group createGroup(Group base) throws RedmineException {
        return transport.addObject(base);
    }

    /**
     * Deletes a group.
     * <p><strong>This operation requires "Redmine Administrator" permission.</strong>
     */
    public void deleteGroup(Group base) throws RedmineException {
        transport.deleteObject(Group.class, base.getId().toString());
    }

    public List<Role> getRoles() throws RedmineException {
        return transport.getObjectsList(Role.class);
    }

    public Role getRoleById(int id) throws RedmineException {
        return transport.getObject(Role.class, id);
    }

    public void update(User obj) throws RedmineException {
        transport.updateObject(obj);
    }

    public void update(Group group) throws RedmineException {
        transport.updateObject(group);
    }
}
