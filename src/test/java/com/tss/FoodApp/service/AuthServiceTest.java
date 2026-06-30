package com.tss.FoodApp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.tss.FoodApp.model.*;
import com.tss.FoodApp.repository.Repository;
import com.tss.FoodApp.exception.AuthenticationException;
import com.tss.FoodApp.exception.DuplicateEntityException;
import java.util.*;

public class AuthServiceTest {
    private Repository<Admin> adminRepo;
    private Repository<Customer> customerRepo;
    private Repository<DeliveryPartner> driverRepo;
    private AuthService authService;

    @BeforeEach
    public void setUp() {
        adminRepo = new InMemoryRepository<>();
        customerRepo = new InMemoryRepository<>();
        driverRepo = new InMemoryRepository<>();
        authService = new AuthService(adminRepo, customerRepo, driverRepo);
    }

    @Test
    public void testRegisterCustomer_Success() {
        Customer customer = authService.registerCustomer("john_doe", "password123", "John Doe", "9876543210", "123 Main St");
        assertNotNull(customer);
        assertEquals("john_doe", customer.getUsername());
        assertEquals("password123", customer.getPassword());
        assertEquals("John Doe", customer.getName());
        assertEquals("9876543210", customer.getPhone());
        assertEquals("123 Main St", customer.getAddress());
        assertTrue(customer.isActive());
        
        Optional<Customer> found = customerRepo.findById(customer.getId());
        assertTrue(found.isPresent());
    }

    @Test
    public void testRegisterCustomer_DuplicateUsername() {
        authService.registerCustomer("john_doe", "password123", "John", "9876543210", "123 Main St");
        assertThrows(DuplicateEntityException.class, () -> {
            authService.registerCustomer("john_doe", "otherpass", "John Different", "9876543210", "456 Side St");
        });
    }

    @Test
    public void testLogin_Success() {
        authService.registerCustomer("john_doe", "password123", "John Doe", "9876543210", "123 St");
        User user = authService.login("john_doe", "password123");
        assertNotNull(user);
        assertEquals("john_doe", user.getUsername());
        assertEquals(Role.CUSTOMER, user.getRole());
    }

    @Test
    public void testLogin_WrongPassword() {
        authService.registerCustomer("john_doe", "password123", "John Doe", "9876543210", "123 St");
        assertThrows(AuthenticationException.class, () -> {
            authService.login("john_doe", "wrongpassword");
        });
    }

    @Test
    public void testLogin_UserNotFound() {
        assertThrows(AuthenticationException.class, () -> {
            authService.login("non_existent", "pass123");
        });
    }

    @Test
    public void testLogin_InactiveUser() {
        Customer customer = authService.registerCustomer("john_doe", "password123", "John Doe", "9876543210", "123 St");
        customer.setActive(false);
        customerRepo.update(customer);

        assertThrows(AuthenticationException.class, () -> {
            authService.login("john_doe", "password123");
        });
    }

    @Test
    public void testSeedDefaultAdmin() {
        authService.seedDefaultAdmin();
        List<Admin> admins = adminRepo.findAll();
        assertEquals(1, admins.size());
        assertEquals("admin", admins.get(0).getUsername());
    }
}

// Simple in-memory repository for unit testing
class InMemoryRepository<T> implements Repository<T> {
    private final Map<String, T> map = new HashMap<>();

    @Override
    public T save(T entity) {
        String id = getEntityId(entity);
        map.put(id, entity);
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(map.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(map.values());
    }

    @Override
    public T update(T entity) {
        String id = getEntityId(entity);
        if (map.containsKey(id)) {
            map.put(id, entity);
            return entity;
        }
        throw new RuntimeException("Not found: " + id);
    }

    @Override
    public boolean deleteById(String id) {
        return map.remove(id) != null;
    }

    private String getEntityId(T entity) {
        try {
            java.lang.reflect.Method method = entity.getClass().getMethod("getId");
            return (String) method.invoke(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
