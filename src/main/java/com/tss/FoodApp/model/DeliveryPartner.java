package com.tss.FoodApp.model;

public class DeliveryPartner extends User {
    private static final long serialVersionUID = 1L;

    private String phone;
    private String vehicleType;
    private boolean isAvailable;

    public DeliveryPartner(Long id, String username, String password, String name, String phone, String vehicleType) {
        super(id, username, password, name, Role.DELIVERY_PARTNER);
        this.phone = phone;
        this.vehicleType = vehicleType;
        this.isAvailable = true;
    }

    public String getPhone() { return phone; }
    public String getVehicleType() { return vehicleType; }
    public boolean isAvailable() { return isAvailable; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    @Override
    public String toString() {
        return super.toString() + " | Phone: " + phone + " | Vehicle: " + vehicleType + " | Available: " + (isAvailable ? "Yes" : "No");
    }
}
