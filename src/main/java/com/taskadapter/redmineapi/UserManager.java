package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport;
import org.apache.http.message.BasicNameValuePair;

import java.util.List;

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
     * Load the list of users on the server.
     * <p><b>This operation requires "Redmine Administrator" permission.</b>
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
     * This does NOT require Admin privileges by default Redmine installation (tested with Redmine 2.0.3).
     */
    public User getUserById(Integer userId) throws RedmineException {
        return transport.getObject(User.class, userId, new BasicNameValuePair(
                "include", "memberships,groups"));
    }

    /**
     * Load the list of groups on the server.
     * <p><b>This operation requires "Redmine Administrator" permission.</b>
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
     * <b>This operation requires "Redmine Administrators" permission.</b>
     *
     * @param id
     *            the id of the group
     * @return the group
     * @throws RedmineException
     */
    public Group getGroupById(int id) throws RedmineException {
        return transport.getObject(Group.class, id);
    }

    /**
     * Returns the group based on its name.
     * <p>
     * <b>This operation requires "Redmine Administrators" permission.</b>
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
     * <p><b>This operation requires "Redmine Administrator" permission.</b>
     * @return created group.
     * @throws RedmineException
     */
    public Group createGroup(Group base) throws RedmineException {
        return transport.addObject(base);
    }

    /**
     * Deletes a group.
     * <p><b>This operation requires "Redmine Administrator" permission.</b>
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
    public List<Membership> getMemberships(String project)
            throws RedmineException {
        return transport.getChildEntries(Project.class, project,
                Membership.class);
    }

    public List<Membership> getMemberships(Project project)
            throws RedmineException {
        return getMemberships(getProjectKey(project));
    }

    /**
     * Add a membership.
     *
     * @param membership
     *            membership.
     * @throws RedmineException
     */
    public void addMembership(Membership membership) throws RedmineException {
        final Project project = membership.getProject();
        if (project == null)
            throw new IllegalArgumentException("Project must be set");
        if (membership.getUser() == null)
            throw new IllegalArgumentException("User must be set");
        transport.addChildEntry(Project.class, getProjectKey(project),
                membership);
    }

    private static String getProjectKey(Project project) {
        return project.getId() != null ? project.getId().toString() : project
                .getIdentifier();
    }

    public Membership getMembership(int id) throws RedmineException {
        return transport.getObject(Membership.class, id);
    }

    public void deleteMembership(int id) throws RedmineException {
        transport.deleteObject(Membership.class, Integer.toString(id));
    }

    public void delete(Membership membership) throws RedmineException {
        transport.deleteObject(Membership.class, membership.getId().toString());
    }

    public void update(User obj) throws RedmineException {
        transport.updateObject(obj);
    }

    public void update(Membership membership) throws RedmineException {
        transport.updateObject(membership);
    }

    public void update(Group group) throws RedmineException {
        transport.updateObject(group);
    }
}
