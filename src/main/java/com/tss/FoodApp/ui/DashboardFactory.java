package com.tss.FoodApp.ui;

import com.tss.FoodApp.model.User;
import com.tss.FoodApp.ServiceRegistry;

public interface DashboardFactory {
    void showDashboard(User user, ServiceRegistry registry);
}
