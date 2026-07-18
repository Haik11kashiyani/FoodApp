package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.Customer;
import com.tss.FoodApp.repository.Repository;

public class CustomerJdbcRepository implements Repository<Customer> {

	private static final String INSERT_SQL = "INSERT INTO customers (username, password, name, phone, address, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
	private static final String SELECT_BY_ID = "SELECT * FROM customers WHERE id = ?";
	private static final String SELECT_ALL = "SELECT * FROM customers ORDER BY id";
	private static final String UPDATE_SQL = "UPDATE customers SET username = ?, password = ?, name = ?, phone = ?, address = ?, is_active = ? WHERE id = ?";
	private static final String DELETE_SQL = "DELETE FROM customers WHERE id = ?";

	@Override
	public Customer save(Customer entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setString(4, entity.getPhone());
			ps.setString(5, entity.getAddress());
			ps.setBoolean(6, entity.isActive());
			ps.setString(7, entity.getCreatedAt());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					entity.setId(id);
					return entity;
				}
			}
			throw new AppException("Failed to insert customer, no id returned.");
		} catch (SQLException e) {
			throw new AppException("Failed to save customer: " + e.getMessage(), e);
		}
	}

	@Override
	public Customer findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Customer c = new Customer(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("phone"), rs.getString("address"));
					if (!rs.getBoolean("is_active")) c.setActive(false);
					return c;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find customer: " + e.getMessage(), e);
		}
	}

	@Override
	public List<Customer> findAll() {
		List<Customer> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Customer c = new Customer(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("phone"), rs.getString("address"));
				if (!rs.getBoolean("is_active")) c.setActive(false);
				list.add(c);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list customers: " + e.getMessage(), e);
		}
	}

	@Override
	public Customer update(Customer entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setString(4, entity.getPhone());
			ps.setString(5, entity.getAddress());
			ps.setBoolean(6, entity.isActive());
			ps.setLong(7, entity.getId());
			int updated = ps.executeUpdate();
			if (updated > 0) return entity;
			throw new AppException("Customer not found for update: " + entity.getId());
		} catch (SQLException e) {
			throw new AppException("Failed to update customer: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean deleteById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new AppException("Failed to delete customer: " + e.getMessage(), e);
		}
	}
}
