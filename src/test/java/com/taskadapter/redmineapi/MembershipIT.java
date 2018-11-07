package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.Group;
import com.taskadapter.redmineapi.bean.GroupFactory;
import com.taskadapter.redmineapi.bean.Membership;
import com.taskadapter.redmineapi.bean.MembershipFactory;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Role;
import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;
import com.taskadapter.redmineapi.internal.ResultsWrapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class MembershipIT {
    private static RedmineManager mgr;
    private static UserManager userManager;
    private static Project project;
    private static ProjectManager projectManager;

    private List<User> testUsers = null;

    @BeforeClass
    public static void oneTimeSetup() {
        mgr = IntegrationTestHelper.createRedmineManager();
        projectManager = mgr.getProjectManager();
        userManager = mgr.getUserManager();
        try {
            project = IntegrationTestHelper.createAndReturnProject(mgr.getProjectManager());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void cleanUpTestData() throws RedmineException {
        if (testUsers != null && !testUsers.isEmpty()) {
            for (User user : testUsers) {
                userManager.deleteUser(user.getId());
            }
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
            projectManager.addUserToProject(project.getId(), createdUser.getId(), roles);
            final User userWithMembership = userManager.getUserById(createdUser.getId());
            assertThat(userWithMembership.getMemberships()).isNotEmpty();
        } finally {
            userManager.deleteUser(createdUser.getId());
        }
    }

    @Test
    public void membershipCanBeSetForGroups() throws RedmineException {
        final List<Role> roles = userManager.getRoles();
        final Collection<Role> rolesToSet = Arrays.asList(roles.get(0));
        final Group group = GroupFactory.create("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = mgr.getUserManager().createGroup(group);

            Membership newMembership = projectManager
                    .addGroupToProject(project.getId(), createdGroup.getId(), rolesToSet);
            List<Membership> memberships = projectManager.getProjectMembers(project.getIdentifier());
            assertThat(memberships).contains(newMembership);
            Integer memberGroupId = newMembership.getGroupId();
            assertThat(memberGroupId).isNotNull();
            assertThat(memberGroupId).isEqualTo(createdGroup.getId());
        } finally {
            mgr.getUserManager().deleteGroup(createdGroup);
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
        final Group group = GroupFactory.create("group" + new Random().nextDouble());
        Group createdGroup = null;
        try {
            createdGroup = mgr.getUserManager().createGroup(group);

            projectManager.addGroupToProject(project.getId(), createdGroup.getId(), roles);
            List<Membership> memberships = projectManager.getProjectMembers(project.getIdentifier());
            assertThat(memberships.get(0).getGroupName()).isEqualTo(createdGroup.getName());
        } finally {
            mgr.getUserManager().deleteGroup(createdGroup);
        }
    }

    @Test
    public void listMembershipsWithOffset() throws RedmineException {
        // Insert some test data
        final List<Role> roles = userManager.getRoles();
        testUsers = new ArrayList<>();
        final List<Membership> memberships = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            User user = UserFactory.create();
            user.setLogin("test-user" + i + "@email.test");
            user.setMail(user.getLogin());
            user.setFirstName("Test" + i);
            user.setLastName("User");
            user = userManager.createUser(user);
            testUsers.add(user);

            memberships.add(projectManager.addUserToProject(
                    project.getId(),
                    user.getId(),
                    Collections.singletonList(roles.get(0))
            ));
        }
        final Set<Integer> membershipIds = memberships.stream()
                .map(Membership::getId)
                .collect(Collectors.toSet());

        // Execute test passing both Params and Map<String, String>
        final List<FunctionThrows<Integer, ResultsWrapper<Membership>>> offsetQueries = Arrays.asList(
                offset -> projectManager.getProjectMembers(project.getId(), Collections.singletonMap("offset", Integer.toString(offset))),
                offset -> projectManager.getProjectMembers(project.getId(), new Params().add("offset", Integer.toString(offset)))
        );

        mgr.setObjectsPerPage(25);
        for (FunctionThrows<Integer, ResultsWrapper<Membership>> query : offsetQueries) {
            // Query first page
            final ResultsWrapper<Membership> membershipsFirstPage = query.apply(0);
            assertThat(membershipsFirstPage.getTotalFoundOnServer()).isEqualTo(50);
            assertThat(membershipsFirstPage.getOffsetOnServer()).isEqualTo(0);
            assertThat(membershipsFirstPage.getResults()).hasSize(25);
            final Set<Integer> firstPageIds = membershipsFirstPage.getResults()
                    .stream()
                    .map(Membership::getId)
                    .collect(Collectors.toSet());
            assertThat(firstPageIds).hasSize(25);
            for (Integer membershipId : firstPageIds) {
                assertThat(membershipIds).contains(membershipId);
            }

            // Query next page
            final ResultsWrapper<Membership> membershipsSecondPage = query.apply(25);
            assertThat(membershipsSecondPage.getTotalFoundOnServer()).isEqualTo(50);
            assertThat(membershipsSecondPage.getOffsetOnServer()).isEqualTo(25);
            assertThat(membershipsSecondPage.getResults()).hasSize(25);
            final Set<Integer> secondPageIds = membershipsSecondPage.getResults()
                    .stream()
                    .map(Membership::getId)
                    .collect(Collectors.toSet());
            assertThat(secondPageIds).hasSize(25);
            for (Integer membershipId : secondPageIds) {
                assertThat(firstPageIds).doesNotContain(membershipId);
                assertThat(membershipIds).contains(membershipId);
            }
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

    @FunctionalInterface
    private interface FunctionThrows<T, R> {
        R apply(T param) throws RedmineException;
    }
}
