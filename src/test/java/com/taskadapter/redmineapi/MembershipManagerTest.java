package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.GroupFactory;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.MembershipFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.fest.assertions.Assertions.assertThat;

public class MembershipManagerTest {
    private static RedmineManager mgr;
    private static UserManager userManager;
    private static MembershipManager membershipManager;
    private static Project project;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        membershipManager = mgr.getMembershipManager();
        userManager = mgr.getUserManager();
        try {
            project = IntegrationTestHelper.createAndReturnProject(mgr.getProjectManager());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IntegrationTestHelper.deleteProject(mgr, project.getIdentifier());
    }

    @Test
    public void membershipCanBeSetForUsers() throws RedmineException {
        final List<Role> roles = userManager.getRoles();
        final User user = UserGenerator.generateRandomUser();
        User createdUser = mgr.getUserManager().createUser(user);
        try {
            membershipManager.createMembershipForUser(project.getId(), createdUser.getId(), roles);
            final User userWithMembership = userManager.getUserById(createdUser.getId());
            assertThat(userWithMembership.getMemberships()).isNotEmpty();
        } finally {
            userManager.deleteUser(createdUser.getId());
        }
    }

    @Test
    public void membershipCanBeSetForGroups() throws RedmineException {
        final List<Role> roles = userManager.getRoles();
        Collection<Role> rolesToSet = Arrays.asList(roles.get(0));

        Group group = GroupFactory.create();
        group.setName("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = mgr.getUserManager().createGroup(group);

            Membership newMembership = membershipManager
                    .createMembershipForGroup(project.getId(), createdGroup.getId(), rolesToSet);
            List<Membership> memberships = membershipManager.getMemberships(project.getIdentifier());
            assertThat(memberships).contains(newMembership);
            Group memberGroup = newMembership.getGroup();
            assertThat(memberGroup).isNotNull();
            assertThat(memberGroup.getId()).isEqualTo(createdGroup.getId());
        } finally {
            mgr.getUserManager().deleteGroup(createdGroup);
        }
    }

    @Test
    public void extraRolesRemovedFromMembership() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();
        final int totalRoles = roles.size();

        final Membership membership = membershipManager.createMembershipForUser(project.getId(),
                                        currentUser.getId(), roles);
        assertThat(membership.getRoles().size()).isEqualTo(totalRoles);

        final Membership membershipWithOnlyOneRole = MembershipFactory.create(membership.getId());
        membershipWithOnlyOneRole.setProject(membership.getProject());
        membershipWithOnlyOneRole.setUser(membership.getUser());
        membershipWithOnlyOneRole.addRoles(Collections.singletonList(roles.get(0)));

        membershipManager.update(membershipWithOnlyOneRole);
        final Membership updatedEmptyMembership = membershipManager.getMembership(membership.getId());

        assertThat(updatedEmptyMembership.getRoles().size()).isEqualTo(1);
        membershipManager.delete(membership);
    }

    @Test
    public void membershipsLoadedByProjectStringKey() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();

        final Membership membershipForUser = membershipManager.createMembershipForUser(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships = membershipManager.getMemberships(project.getIdentifier());
        verifyMemberships(roles, currentUser, memberships);
        membershipManager.delete(membershipForUser);
    }

    @Test
    public void membershipsLoadedByProjectId() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();
        final User currentUser = mgr.getUserManager().getCurrentUser();

        final Membership membershipForUser = membershipManager.createMembershipForUser(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships = membershipManager.getMemberships(project.getId());
        verifyMemberships(roles, currentUser, memberships);
        membershipManager.delete(membershipForUser);
    }

    private void verifyMemberships(List<Role> roles, User currentUser, List<Membership> memberships) throws RedmineException {
        assertThat(memberships.size()).isEqualTo(1);
        final Membership membership = memberships.get(0);
        assertThat(membership.getUser().getId()).isEqualTo(currentUser.getId());
        assertThat(membership.getRoles().size()).isEqualTo(roles.size());

        final Membership membershipById = membershipManager.getMembership(membership.getId());
        assertThat(membershipById).isEqualTo(membership);
    }
}
