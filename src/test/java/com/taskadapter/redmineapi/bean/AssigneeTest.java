
package com.taskadapter.redmineapi.bean;

import org.junit.Assert;
import org.junit.Test;

public class AssigneeTest {
    @Test
    public void testAssigneeEquivalent() {
        Assignee demoAssignee = GenericAssigneeFactory.createGenericAssignee(1, "Demo Assignee");
        Group group = GroupFactory.create(1);
        group.setName("Testgroup");
        User user = UserFactory.create(1);
        user.setFullName("A testuser");
        
        assert demoAssignee.equals(group);
        assert group.equals(demoAssignee);
        assert demoAssignee.equals(user);
        assert user.equals(demoAssignee);
        assert user.equals(group);
        assert group.equals(user);
    }
}
