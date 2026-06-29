package com.tss.FoodApp.model;

import com.tss.FoodApp.enums.Role;

public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin(String id, String username, String password, String name) {
        super(id, username, password, name, Role.ADMIN);
    }
}