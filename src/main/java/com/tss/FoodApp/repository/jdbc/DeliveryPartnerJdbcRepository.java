package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.DeliveryPartner;
import com.tss.FoodApp.repository.Repository;

public class DeliveryPartnerJdbcRepository implements Repository<DeliveryPartner> {

	private static final String INSERT_SQL = "INSERT INTO delivery_partners (username, password, name, phone, vehicle_type, is_available, is_active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
	private static final String SELECT_BY_ID = "SELECT * FROM delivery_partners WHERE id = ?";
	private static final String SELECT_ALL = "SELECT * FROM delivery_partners ORDER BY id";
	private static final String UPDATE_SQL = "UPDATE delivery_partners SET username = ?, password = ?, name = ?, phone = ?, vehicle_type = ?, is_available = ?, is_active = ? WHERE id = ?";
	private static final String DELETE_SQL = "DELETE FROM delivery_partners WHERE id = ?";

	@Override
	public DeliveryPartner save(DeliveryPartner entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setString(4, entity.getPhone());
			ps.setString(5, entity.getVehicleType());
			ps.setBoolean(6, entity.isAvailable());
			ps.setBoolean(7, entity.isActive());
			ps.setString(8, entity.getCreatedAt());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					long id = rs.getLong(1);
					entity.setId(id);
					return entity;
				}
			}
			throw new AppException("Failed to insert driver, no id returned.");
		} catch (SQLException e) {
			throw new AppException("Failed to save driver: " + e.getMessage(), e);
		}
	}

	@Override
	public DeliveryPartner findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					DeliveryPartner d = new DeliveryPartner(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("phone"), rs.getString("vehicle_type"));
					if (!rs.getBoolean("is_active")) d.setActive(false);
					d.setAvailable(rs.getBoolean("is_available"));
					return d;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find driver: " + e.getMessage(), e);
		}
	}

	@Override
	public List<DeliveryPartner> findAll() {
		List<DeliveryPartner> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				DeliveryPartner d = new DeliveryPartner(rs.getLong("id"), rs.getString("username"), rs.getString("password"), rs.getString("name"), rs.getString("phone"), rs.getString("vehicle_type"));
				if (!rs.getBoolean("is_active")) d.setActive(false);
				d.setAvailable(rs.getBoolean("is_available"));
				list.add(d);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list drivers: " + e.getMessage(), e);
		}
	}

	@Override
	public DeliveryPartner update(DeliveryPartner entity) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
			ps.setString(1, entity.getUsername());
			ps.setString(2, entity.getPassword());
			ps.setString(3, entity.getName());
			ps.setString(4, entity.getPhone());
			ps.setString(5, entity.getVehicleType());
			ps.setBoolean(6, entity.isAvailable());
			ps.setBoolean(7, entity.isActive());
			ps.setLong(8, entity.getId());
			int updated = ps.executeUpdate();
			if (updated > 0) return entity;
			throw new AppException("Driver not found for update: " + entity.getId());
		} catch (SQLException e) {
			throw new AppException("Failed to update driver: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean deleteById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new AppException("Failed to delete driver: " + e.getMessage(), e);
		}
	}
}
