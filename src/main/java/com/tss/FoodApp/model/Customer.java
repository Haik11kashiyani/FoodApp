package com.tss.FoodApp.model;

public class Customer extends User {
    private static final long serialVersionUID = 1L;

    private String address;
    private String phone;

    public Customer(Long id, String username, String password, String name, String phone, String address) {
        super(id, username, password, name, Role.CUSTOMER);
        this.phone = phone;
        this.address = address;
    }

    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public void setAddress(String address) { this.address = address; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return super.toString() + " | Phone: " + phone + " | Address: " + address;
    }
}
