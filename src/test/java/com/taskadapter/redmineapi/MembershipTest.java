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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MembershipTest {
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
        assertTrue(userWithMembership.getMemberships().size() > 0);
        } finally {
            userManager.deleteUser(createdUser.getId());
        }
    }

    /**
     * This test fails on Redmine 2.5.2: apparently, it does not support adding group memberships.
     * I submitted bug http://www.redmine.org/issues/17904
     */
    @Ignore("Bug in Redmine 2.5.2: can't add a group membership. see http://www.redmine.org/issues/17904")
    @Test
    public void membershipCanBeSetForGroups() throws RedmineException {
        final List<Role> roles = userManager.getRoles();
        Collection<Role> rolesToSet = Arrays.asList(roles.get(0));

        Group group = GroupFactory.create();
        group.setName("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = mgr.getUserManager().createGroup(group);

            membershipManager.createMembershipForGroup(project.getId(), createdGroup.getId(), rolesToSet);
            // TODO add assert here
            List<Membership> memberships = membershipManager.getMemberships(project.getIdentifier());
            System.out.println(memberships);
        } finally {
            mgr.getUserManager().deleteGroup(createdGroup);
        }
    }

    @Test
    public void testMemberships() throws RedmineException {
        final List<Role> roles = mgr.getUserManager().getRoles();

        final User currentUser = mgr.getUserManager().getCurrentUser();

        membershipManager.createMembershipForUser(project.getId(), currentUser.getId(), roles);
        final List<Membership> memberships1 = membershipManager.getMemberships(project.getIdentifier());
        assertEquals(1, memberships1.size());
        final Membership createdMembership = memberships1.get(0);
        assertEquals(currentUser.getId(), createdMembership.getUser()
                .getId());
        assertEquals(roles.size(), createdMembership.getRoles().size());

        final Membership membershipById = membershipManager.getMembership(createdMembership
                .getId());
        assertEquals(createdMembership, membershipById);

        final Membership emptyMembership = MembershipFactory.create(createdMembership.getId());
        emptyMembership.setProject(createdMembership.getProject());
        emptyMembership.setUser(createdMembership.getUser());
        emptyMembership.addRoles(Collections.singletonList(roles.get(0)));

        membershipManager.update(emptyMembership);
        final Membership updatedEmptyMembership = membershipManager.getMembership(createdMembership.getId());

        assertEquals(1, updatedEmptyMembership.getRoles().size());
        membershipManager.delete(updatedEmptyMembership);
    }

}
