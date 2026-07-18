package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.Cart;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.repository.Repository;

public class CartJdbcRepository implements Repository<Cart> {

    private static final String INSERT_SQL = "INSERT INTO carts (customer_id, created_at) VALUES (?, ?)";
    private static final String SELECT_BY_ID = "SELECT c.customer_id, c.created_at, ci.menu_item_id, ci.item_name, ci.price, ci.quantity FROM carts c LEFT JOIN cart_items ci ON c.customer_id = ci.customer_id WHERE c.customer_id = ?";
    private static final String UPDATE_SQL = "UPDATE carts SET created_at = ? WHERE customer_id = ?";
    private static final String DELETE_SQL = "DELETE FROM carts WHERE customer_id = ?";

    private String serializeItems(List<CartItem> items) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            String name = it.getItemName().replace("|", "?").replace(":", "?");
            sb.append(it.getMenuItemId()).append(":").append(it.getPrice()).append(":").append(it.getQuantity()).append(":").append(name);
            if (i < items.size() - 1) sb.append("|");
        }
        return sb.toString();
    }

    private List<CartItem> deserializeItems(String raw) {
        List<CartItem> list = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return list;
        String[] parts = raw.split("\\|");
        for (String p : parts) {
            String[] f = p.split(":", 4);
            if (f.length < 4) continue;
            Long menuId = Long.parseLong(f[0]);
            double price = Double.parseDouble(f[1]);
            int qty = Integer.parseInt(f[2]);
            String name = f[3].replace("?", ":");
            list.add(new CartItem(menuId, name, price, qty));
        }
        return list;
    }

    @Override
    public Cart save(Cart entity) {
        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
            ps.setLong(1, entity.getId());
            ps.setString(2, entity.getCreatedAt());
            ps.executeUpdate();

            // insert cart items
            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                try (PreparedStatement ips = con.prepareStatement("INSERT INTO cart_items (customer_id, menu_item_id, item_name, price, quantity) VALUES (?, ?, ?, ?, ?)")) {
                    for (CartItem it : entity.getItems()) {
                        ips.setLong(1, entity.getId());
                        if (it.getMenuItemId() == null) ips.setObject(2, null); else ips.setLong(2, it.getMenuItemId());
                        ips.setString(3, it.getItemName());
                        ips.setDouble(4, it.getPrice());
                        ips.setInt(5, it.getQuantity());
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new AppException("Failed to save cart: " + e.getMessage(), e);
        }
    }

    @Override
    public Cart findById(Long id) {
        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                Cart c = null;
                while (rs.next()) {
                    if (c == null) {
                        c = new Cart(rs.getLong("customer_id"));
                        c.setCreatedAt(rs.getString("created_at"));
                    }
                    Long menuId = rs.getObject("menu_item_id") == null ? null : rs.getLong("menu_item_id");
                    double price = rs.getObject("price") == null ? 0.0 : rs.getDouble("price");
                    int qty = rs.getObject("quantity") == null ? 0 : rs.getInt("quantity");
                    String name = rs.getString("item_name");
                    if (name != null) c.getItems().add(new CartItem(menuId, name, price, qty));
                }
                return c;
            }
        } catch (SQLException e) {
            throw new AppException("Failed to find cart: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Cart> findAll() {
        throw new UnsupportedOperationException("findAll not needed for carts");
    }

    @Override
    public Cart update(Cart entity) {
        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
            ps.setString(1, entity.getCreatedAt());
            ps.setLong(2, entity.getId());
            int updated = ps.executeUpdate();
            if (updated <= 0) {
                // if no row, try insert
                return save(entity);
            }

            // delete existing cart_items and re-insert
            try (PreparedStatement dps = con.prepareStatement("DELETE FROM cart_items WHERE customer_id = ?")) {
                dps.setLong(1, entity.getId());
                dps.executeUpdate();
            }
            if (entity.getItems() != null && !entity.getItems().isEmpty()) {
                try (PreparedStatement ips = con.prepareStatement("INSERT INTO cart_items (customer_id, menu_item_id, item_name, price, quantity) VALUES (?, ?, ?, ?, ?)")) {
                    for (CartItem it : entity.getItems()) {
                        ips.setLong(1, entity.getId());
                        if (it.getMenuItemId() == null) ips.setObject(2, null); else ips.setLong(2, it.getMenuItemId());
                        ips.setString(3, it.getItemName());
                        ips.setDouble(4, it.getPrice());
                        ips.setInt(5, it.getQuantity());
                        ips.addBatch();
                    }
                    ips.executeBatch();
                }
            }
            return entity;
        } catch (SQLException e) {
            throw new AppException("Failed to update cart: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(Long id) {
        try (Connection con = JdbcUtil.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new AppException("Failed to delete cart: " + e.getMessage(), e);
        }
    }
}
