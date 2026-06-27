package com.tss.FoodApp.Model.Users;

public abstract class Users {
    protected String id;
    protected String name;
    protected String phoneNumber;
    protected String role;

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }


    abstract void login();
    abstract void createUser();
}
