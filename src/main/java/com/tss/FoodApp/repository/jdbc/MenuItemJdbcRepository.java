package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.MenuItem;
import com.tss.FoodApp.model.FoodCategory;
import com.tss.FoodApp.model.CuisineType;
import com.tss.FoodApp.repository.Repository;

public class MenuItemJdbcRepository implements Repository<MenuItem> {

	private static final String INSERT_SQL = "INSERT INTO menu_items (name, price, category, cuisine_type, is_available, created_at) VALUES (?, ?, ?, ?, ?, ?) RETURNING id";
	private static final String SELECT_BY_ID = "SELECT * FROM menu_items WHERE id = ?";
	private static final String SELECT_ALL = "SELECT * FROM menu_items ORDER BY id";
	private static final String UPDATE_SQL = "UPDATE menu_items SET name = ?, price = ?, category = ?, cuisine_type = ?, is_available = ? WHERE id = ?";
	private static final String DELETE_SQL = "DELETE FROM menu_items WHERE id = ?";

	@Override
	public MenuItem save(MenuItem entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
			ps.setString(1, entity.getName());
			ps.setDouble(2, entity.getPrice());
			ps.setString(3, entity.getCategory().name());
			ps.setString(4, entity.getCuisineType().name());
			ps.setBoolean(5, entity.isAvailable());
			ps.setString(6, entity.getCreatedAt());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					entity.setId(id);
					return entity;
				}
			}
			throw new AppException("Failed to insert menu item, no id returned.");
		} catch (SQLException e) {
			throw new AppException("Failed to save menu item: " + e.getMessage(), e);
		}
	}

	@Override
	public MenuItem findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					MenuItem m = new MenuItem(rs.getLong("id"), rs.getString("name"), rs.getDouble("price"), FoodCategory.valueOf(rs.getString("category")), CuisineType.valueOf(rs.getString("cuisine_type")));
					m.setAvailable(rs.getBoolean("is_available"));
					return m;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find menu item: " + e.getMessage(), e);
		}
	}

	@Override
	public List<MenuItem> findAll() {
		List<MenuItem> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				MenuItem m = new MenuItem(rs.getLong("id"), rs.getString("name"), rs.getDouble("price"), FoodCategory.valueOf(rs.getString("category")), CuisineType.valueOf(rs.getString("cuisine_type")));
				m.setAvailable(rs.getBoolean("is_available"));
				list.add(m);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list menu items: " + e.getMessage(), e);
		}
	}

	@Override
	public MenuItem update(MenuItem entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
			ps.setString(1, entity.getName());
			ps.setDouble(2, entity.getPrice());
			ps.setString(3, entity.getCategory().name());
			ps.setString(4, entity.getCuisineType().name());
			ps.setBoolean(5, entity.isAvailable());
			ps.setLong(6, entity.getId());
			int updated = ps.executeUpdate();
			if (updated > 0) return entity;
			throw new AppException("Menu item not found for update: " + entity.getId());
		} catch (SQLException e) {
			throw new AppException("Failed to update menu item: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean deleteById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new AppException("Failed to delete menu item: " + e.getMessage(), e);
		}
	}
}
