package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.GroupFactory;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.MembershipFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.ProjectFactory;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Collection;
import java.util.List;

public class MembershipManager {
    private final Transport transport;

    MembershipManager(Transport transport) {
        this.transport = transport;
    }

    public List<Membership> getMemberships(String projectKey)
            throws RedmineException {
        return transport.getChildEntries(Project.class, projectKey, Membership.class);
    }

    /**
     * Add a membership.
     *
     * @param membership
     *            membership.
     * @throws RedmineException
     */
    private void addMembership(Membership membership) throws RedmineException {
        final Project project = membership.getProject();
        if (project == null) {
            throw new IllegalArgumentException("Project must be set");
        }
        if (membership.getUser() == null && membership.getRoles().isEmpty()) {
            throw new IllegalArgumentException("Either User or Roles field must be set");
        }
        transport.addChildEntry(Project.class, project.getId()+"", membership);
    }

    public Membership getMembership(int membershipId) throws RedmineException {
        return transport.getObject(Membership.class, membershipId);
    }

    public void delete(int membershipId) throws RedmineException {
        transport.deleteObject(Membership.class, Integer.toString(membershipId));
    }

    public void delete(Membership membership) throws RedmineException {
        transport.deleteObject(Membership.class, membership.getId().toString());
    }

    public void update(Membership membership) throws RedmineException {
        transport.updateObject(membership);
    }

    /**
     * THIS DOES NOT WORK YET!
     * fails on Redmine 2.5.2: apparently, it does not support adding group memberships.
     * I submitted bug http://www.redmine.org/issues/17904
     *
     * This method is here only to show what the future API will look like
     */
    @Deprecated
    public void createMembershipForGroup(int projectId, int groupId, Collection<Role> roles) throws RedmineException {
        final Membership membership = MembershipFactory.create();
        final Project project = ProjectFactory.create(projectId);
        membership.setProject(project);
        membership.setGroup(GroupFactory.create(groupId));
        membership.addRoles(roles);

        addMembership(membership);
    }

    public void createMembershipForUser(int projectId, int userId, Collection<Role> roles) throws RedmineException {
        final Membership membership = MembershipFactory.create();
        final Project project = ProjectFactory.create(projectId);
        membership.setProject(project);
        membership.setUser(UserFactory.create(userId));
        membership.addRoles(roles);
        addMembership(membership);
    }
}
