package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.bean.UserFactory;

import java.util.Date;

public class UserGenerator {
    public static User generateRandomUser() {
        long randomNumber = new Date().getTime();
        return UserFactory.create()
                .setFirstName("fname")
                .setLastName("lname")
                .setLogin("login" + randomNumber)
                .setMail("somemail" + randomNumber + "@somedomain.com")
                .setPassword("zzzz1234");
    }
}
