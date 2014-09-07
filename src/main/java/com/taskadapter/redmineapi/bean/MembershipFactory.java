package com.taskadapter.redmineapi.bean;

public class MembershipFactory {
    public static Membership create(Integer id) {
        return new Membership(id);
    }

    public static Membership create() {
        return new Membership(null);
    }
}
