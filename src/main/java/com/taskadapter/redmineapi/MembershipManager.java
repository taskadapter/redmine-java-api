package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;
import java.util.List;

/**
 * Deprecated - methods from this class were integrated into ProjectManager class.
 *
 * Works with User-Project Memberships.
 * <p>Obtain it via RedmineManager:
 * <pre>
 RedmineManager mgr = RedmineManagerFactory.createWithUserAuth(redmineURI, login, password);
 MembershipManager membershipManager = mgr.getMembershipManager();
 * </pre>
 *
 * <p>Sample usage:
 * <pre>
 roles = mgr.getUserManager().getRoles();
 currentUser = mgr.getUserManager().getCurrentUser();
 final Membership membershipForUser = membershipManager.createMembershipForUser(project.getId(), currentUser.getId(), roles);
 memberships = membershipManager.getMemberships(project.getId());
 membershipManager.delete(membershipForUser);
 * </pre>
 *
 * @see RedmineManager
 */
@Deprecated
public class MembershipManager {
    private final Transport transport;

    MembershipManager(Transport transport) {
        this.transport = transport;
    }

    /**
     * Use {@link ProjectManager#getProjectMembers(String)} instead.
     */
    @Deprecated
    public List<Membership> getMemberships(String projectKey)
            throws RedmineException {
        return transport.getChildEntries(Project.class, projectKey, Membership.class);
    }

    /**
     * Use {@link ProjectManager#getProjectMembers(int)} instead.
     */
    @Deprecated
    public List<Membership> getMemberships(int projectId) throws RedmineException {
        return transport.getChildEntries(Project.class, projectId, Membership.class);
    }

    private Membership addMembership(Membership membership) throws RedmineException {
        final Project project = membership.getProject();
        if (project == null) {
            throw new IllegalArgumentException("Project must be set");
        }
        if (membership.getUserId() == null && membership.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Either User or Roles field must be set");
        }
        return transport.addChildEntry(Project.class, project.getId()+"", membership);
    }

    public Membership getMembership(int membershipId) throws RedmineException {
        return transport.getObject(Membership.class, membershipId);
    }

    /**
     * DEPRECATED. use membership.delete() instead
     */
    @Deprecated
    public void delete(int membershipId) throws RedmineException {
        transport.deleteObject(Membership.class, Integer.toString(membershipId));
    }

    /**
     * DEPRECATED. use membership.delete() instead
     */
    @Deprecated
    public void delete(Membership membership) throws RedmineException {
        transport.deleteObject(Membership.class, membership.getId().toString());
    }

    /**
     * DEPRECATED. use membership.update() instead
     */
    @Deprecated
    public void update(Membership membership) throws RedmineException {
        transport.updateObject(membership);
    }


    /**
     * Use membership.create() instead.
     */
    @Deprecated
    public Membership createMembershipForGroup(int projectId, int itemId, Collection<Role> roles) throws RedmineException {
        Project project = new Project(transport).setId(projectId);

        Membership membership = new Membership(transport)
                .setProject(project)
                .setUserId(itemId)
                .addRoles(roles);

        return addMembership(membership);
    }

    /**
     * Use membership.create() instead.
     */
    @Deprecated
    public Membership createMembershipForUser(int projectId, int itemId, Collection<Role> roles) throws RedmineException {
        // according to the documentation, this the way it is supposed to work : user Id is same as group ID
        // in this context. see:
        // http://www.redmine.org/projects/redmine/wiki/Rest_Memberships#POST
        // http://www.redmine.org/issues/17904
        return createMembershipForGroup(projectId, itemId, roles);
    }
}
