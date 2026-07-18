package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.Admin;
import com.tss.FoodApp.repository.Repository;

public class AdminJdbcRepository implements Repository<Admin> {

	private static final String INSERT_SQL = "INSERT INTO admins (username, password, name, is_active, created_at) VALUES (?, ?, ?, ?, ?) RETURNING id";
	private static final String SELECT_BY_ID = "SELECT * FROM admins WHERE id = ?";
	private static final String SELECT_ALL = "SELECT * FROM admins ORDER BY id";
	private static final String UPDATE_SQL = "UPDATE admins SET username = ?, password = ?, name = ?, is_active = ? WHERE id = ?";
	private static final String DELETE_SQL = "DELETE FROM admins WHERE id = ?";

	@Override
	public Admin save(Admin entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setBoolean(4, entity.isActive());
			ps.setString(5, entity.getCreatedAt());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					entity.setId(id);
					return entity;
				}
			}
			throw new AppException("Failed to insert admin, no id returned.");
		} catch (SQLException e) {
			throw new AppException("Failed to save admin: " + e.getMessage(), e);
		}
	}

	@Override
	public Admin findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Admin a = new Admin(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"));
					if (!rs.getBoolean("is_active")) a.setActive(false);
					return a;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find admin: " + e.getMessage(), e);
		}
	}

	@Override
	public List<Admin> findAll() {
		List<Admin> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Admin a = new Admin(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"));
				if (!rs.getBoolean("is_active")) a.setActive(false);
				list.add(a);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list admins: " + e.getMessage(), e);
		}
	}

	@Override
	public Admin update(Admin entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setBoolean(4, entity.isActive());
			ps.setLong(5, entity.getId());
			int updated = ps.executeUpdate();
			if (updated > 0) return entity;
			throw new AppException("Admin not found for update: " + entity.getId());
		} catch (SQLException e) {
			throw new AppException("Failed to update admin: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean deleteById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new AppException("Failed to delete admin: " + e.getMessage(), e);
		}
	}
}
