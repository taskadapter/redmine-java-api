package com.taskadapter.redmineapi;

import com.taskadapter.redmineapi.bean.User;
import com.taskadapter.redmineapi.internal.Transport;

import java.util.Date;

public class UserGenerator {
    public static User generateRandomUser(Transport transport) {
        long randomNumber = new Date().getTime();
        return new User(transport)
                .setFirstName("fname")
                .setLastName("lname")
                .setLogin("login" + randomNumber)
                .setMail("somemail" + randomNumber + "@somedomain.com")
                .setPassword("zzzz1234");
    }
}
