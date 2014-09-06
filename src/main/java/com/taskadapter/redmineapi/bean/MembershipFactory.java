package com.taskadapter.redmineapi.bean;

public class MembershipFactory {
    /**
     * @param id database ID.
     */
    public static Membership create(Integer id) {
        return new Membership(id);
    }

    public static Membership create() {
        return new Membership(null);
    }
}
