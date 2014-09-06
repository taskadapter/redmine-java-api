package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;

import java.util.Date;

public class UserGenerator {
    public static User generateRandomUser() {
        User user = UserFactory.create();
        user.setFirstName("fname");
        user.setLastName("lname");
        long randomNumber = new Date().getTime();
        user.setLogin("login" + randomNumber);
        user.setMail("somemail" + randomNumber + "@somedomain.com");
        user.setPassword("zzzz1234");
        return user;
    }
}
