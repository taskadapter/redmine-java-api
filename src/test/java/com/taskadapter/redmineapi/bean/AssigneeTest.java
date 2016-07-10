package com.taskadapter.redmineapi.bean;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AssigneeTest {
    @Test
    public void testAssigneeEquivalent() {
        Assignee demoAssignee = new GenericAssignee(1, "Demo Assignee");
        Group group = GroupFactory.create(1);
        group.setName("Testgroup");
        User user = UserFactory.create(1);
        user.setFullName("A testuser");
        
        assertThat(demoAssignee).isEqualTo(group);
        assertThat(group).isEqualTo(demoAssignee);

        assertThat(demoAssignee).isEqualTo(user);
        assertThat(user).isEqualTo(demoAssignee);

        assertThat(user).isEqualTo(group);
        assertThat(group).isEqualTo(user);
    }
}
