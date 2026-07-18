package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.User;
import com.tss.FoodApp.repository.Repository;

// Lightweight generic user repository is not used directly but provided for completeness
public class UserJdbcRepository implements Repository<User> {

	private static final String SELECT_USERS =
			"SELECT id, username, password, name, 'ADMIN' as role, is_active, created_at FROM admins UNION ALL " +
			"SELECT id, username, password, name, 'CUSTOMER' as role, is_active, created_at FROM customers UNION ALL " +
			"SELECT id, username, password, name, 'DELIVERY_PARTNER' as role, is_active, created_at FROM delivery_partners";

	@Override
	public User save(User entity) {
		throw new UnsupportedOperationException("Generic User save not supported");
	}

	@Override
	public User findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_USERS + " WHERE id = ?")) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					String role = rs.getString("role");
					User u = new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), com.tss.FoodApp.model.Role.valueOf(role));
					if (!rs.getBoolean("is_active")) u.setActive(false);
					return u;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find user: " + e.getMessage(), e);
		}
	}

	@Override
	public List<User> findAll() {
		List<User> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_USERS);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String role = rs.getString("role");
				User u = new User(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), com.tss.FoodApp.model.Role.valueOf(role));
				if (!rs.getBoolean("is_active")) u.setActive(false);
				list.add(u);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list users: " + e.getMessage(), e);
		}
	}

	@Override
	public User update(User entity) {
		throw new UnsupportedOperationException("Generic User update not supported");
	}

	@Override
	public boolean deleteById(Long id) {
		throw new UnsupportedOperationException("Generic User delete not supported");
	}
}
