package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.MembershipFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class MembershipIT {
    private static RedmineManager mgr;
    private static UserManager userManager;
    private static Project project;
    private static ProjectManager projectManager;
    private static Transport transport;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        transport = mgr.getTransport();
        projectManager = mgr.getProjectManager();
        userManager = mgr.getUserManager();
        try {
            project = IntegrationTestHelper.createProject(mgr.getTransport());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        try {
            project.delete();
        } catch (RedmineException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void membershipCanBeSetForUsers() throws RedmineException {
        List<Role> roles = userManager.getRoles();
        User user = UserGenerator.generateRandomUser(transport);
        User createdUser = user.create();
        try {
            projectManager.addUserToProject(project.getId(), createdUser.getId(), roles);
            final User userWithMembership = userManager.getUserById(createdUser.getId());
            assertThat(userWithMembership.getMemberships()).isNotEmpty();
        } finally {
            createdUser.delete();
        }
    }

    @Test
    public void membershipCanBeSetForGroups() throws RedmineException {
        final List<Role> roles = userManager.getRoles();
        final Collection<Role> rolesToSet = Arrays.asList(roles.get(0));
        final Group group = new Group(transport).setName("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = group.create();

            Membership newMembership = projectManager
                    .addGroupToProject(project.getId(), createdGroup.getId(), rolesToSet);
            List<Membership> memberships = projectManager.getProjectMembers(project.getIdentifier());
            assertThat(memberships).contains(newMembership);
            Integer memberGroupId = newMembership.getGroupId();
            assertThat(memberGroupId).isNotNull();
            assertThat(memberGroupId).isEqualTo(createdGroup.getId());
        } finally {
            createdGroup.delete();
        }
    }

    @Test
    public void extraRolesRemovedFromMembership() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();
        final int totalRoles = roles.size();

        final Membership membership = projectManager.addUserToProject(project.getId(),
                                        currentUser.getId(), roles);
        assertThat(membership.getRoles().size()).isEqualTo(totalRoles);

        final Membership membershipWithOnlyOneRole = MembershipFactory.create(membership.getId());
        membershipWithOnlyOneRole.setProject(membership.getProject());
        membershipWithOnlyOneRole.setUserId(membership.getUserId());
        membershipWithOnlyOneRole.addRoles(Collections.singletonList(roles.get(0)));

        projectManager.updateProjectMembership(membershipWithOnlyOneRole);
        final Membership updatedEmptyMembership = projectManager.getProjectMember(membership.getId());

        assertThat(updatedEmptyMembership.getRoles().size()).isEqualTo(1);
        projectManager.deleteProjectMembership(membership);
    }

    @Test
    public void membershipsLoadedByProjectStringKey() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();

        final Membership membershipForUser = projectManager.addUserToProject(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships = projectManager.getProjectMembers(project.getIdentifier());
        verifyMemberships(roles, currentUser, memberships);
        projectManager.deleteProjectMembership(membershipForUser);
    }

    @Test
    public void membershipsLoadedByProjectId() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();

        final Membership membershipForUser = projectManager.addUserToProject(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships = projectManager.getProjectMembers(project.getId());
        verifyMemberships(roles, currentUser, memberships);
        projectManager.deleteProjectMembership(membershipForUser);
    }

    @Test
    public void membershipsContainUserName() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();

        final Membership membershipForUser = projectManager.addUserToProject(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships = projectManager.getProjectMembers(project.getId());
        assertThat(memberships.get(0).getUserName()).isEqualTo(currentUser.getFullName());
        projectManager.deleteProjectMembership(membershipForUser);
    }
    
    @Test
    public void membershipsContainGoupName() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final Group group = new Group(transport).setName("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = group.create();

            projectManager.addGroupToProject(project.getId(), createdGroup.getId(), roles);
            List<Membership> memberships = projectManager.getProjectMembers(project.getIdentifier());
            assertThat(memberships.get(0).getGroupName()).isEqualTo(createdGroup.getName());
        } finally {
            createdGroup.delete();
        }
    }
    
    private void verifyMemberships(List<Role> roles, User currentUser, List<Membership> memberships) throws RedmineException {
        assertThat(memberships.size()).isEqualTo(1);
        final Membership membership = memberships.get(0);
        assertThat(membership.getUserId()).isEqualTo(currentUser.getId());
        assertThat(membership.getRoles().size()).isEqualTo(roles.size());

        final Membership membershipById = projectManager.getProjectMember(membership.getId());
        assertThat(membershipById).isEqualTo(membership);
    }
}
