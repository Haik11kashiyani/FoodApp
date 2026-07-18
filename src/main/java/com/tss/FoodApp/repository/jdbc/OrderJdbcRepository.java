package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.tss.FoodApp.exception.AppException;
import com.tss.FoodApp.model.CartItem;
import com.tss.FoodApp.model.Order;
import com.tss.FoodApp.model.PaymentMode;
import com.tss.FoodApp.model.OrderStatus;
import com.tss.FoodApp.repository.Repository;

public class OrderJdbcRepository implements Repository<Order> {

	private static final String INSERT_SQL = "INSERT INTO orders (customer_id, customer_name, total_amount, discount_amount, final_amount, payment_mode, delivery_partner_id, delivery_partner_name, status, ordered_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
	private static final String SELECT_BY_ID = "SELECT * FROM orders WHERE id = ?";
	private static final String SELECT_ALL = "SELECT * FROM orders ORDER BY id";
	private static final String UPDATE_SQL = "UPDATE orders SET customer_id = ?, customer_name = ?, total_amount = ?, discount_amount = ?, final_amount = ?, payment_mode = ?, delivery_partner_id = ?, delivery_partner_name = ?, status = ? WHERE id = ?";
	private static final String DELETE_SQL = "DELETE FROM orders WHERE id = ?";

	private static final String INSERT_ORDER_ITEM = "INSERT INTO order_items (order_id, menu_item_id, item_name, price, quantity) VALUES (?, ?, ?, ?, ?)";
	private static final String SELECT_ORDER_ITEMS = "SELECT * FROM order_items WHERE order_id = ? ORDER BY id";

	private List<CartItem> loadOrderItems(Connection con, Long orderId) throws SQLException {
		List<CartItem> items = new ArrayList<>();
		try (PreparedStatement ps = con.prepareStatement(SELECT_ORDER_ITEMS)) {
			ps.setLong(1, orderId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Long menuId = rs.getObject("menu_item_id") == null ? null : rs.getLong("menu_item_id");
					double price = rs.getDouble("price");
					int qty = rs.getInt("quantity");
					String name = rs.getString("item_name");
					items.add(new CartItem(menuId, name, price, qty));
				}
			}
		}
		return items;
	}

	@Override
	public Order save(Order entity) {
		Connection con = null;
		try {
			con = JdbcUtil.getConnection();
			con.setAutoCommit(false);

			try (PreparedStatement ps = con.prepareStatement(INSERT_SQL)) {
				ps.setLong(1, entity.getCustomerId());
				ps.setString(2, entity.getCustomerName());
				ps.setDouble(3, entity.getTotalAmount());
				ps.setDouble(4, entity.getDiscountAmount());
				ps.setDouble(5, entity.getFinalAmount());
				ps.setString(6, entity.getPaymentMode().name());
				if (entity.getDeliveryPartnerId() == null) ps.setObject(7, null); else ps.setLong(7, entity.getDeliveryPartnerId());
				ps.setString(8, entity.getDeliveryPartnerName());
				ps.setString(9, entity.getStatus().name());
				ps.setString(10, entity.getOrderedAt());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						long orderId = rs.getLong(1);
						entity.setId(orderId);

						// insert order items
						try (PreparedStatement ips = con.prepareStatement(INSERT_ORDER_ITEM)) {
							for (CartItem it : entity.getItems()) {
								if (it.getMenuItemId() == null) ips.setObject(2, null); else ips.setLong(2, it.getMenuItemId());
								ips.setLong(1, orderId);
								ips.setString(3, it.getItemName());
								ips.setDouble(4, it.getPrice());
								ips.setInt(5, it.getQuantity());
								ips.addBatch();
							}
							ips.executeBatch();
						}

						con.commit();
						return entity;
					}
				}
			}
			throw new AppException("Failed to insert order, no id returned.");
		} catch (SQLException e) {
			if (con != null) try { con.rollback(); } catch (SQLException ignore) {}
			throw new AppException("Failed to save order: " + e.getMessage(), e);
		} finally {
			if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignore) {}
		}
	}

	@Override
	public Order findById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_BY_ID)) {
			ps.setLong(1, id);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					Order o = new Order(rs.getLong("id"), rs.getLong("customer_id"), rs.getString("customer_name"), new ArrayList<>(), rs.getDouble("total_amount"), rs.getDouble("discount_amount"), rs.getDouble("final_amount"), PaymentMode.valueOf(rs.getString("payment_mode")), rs.getObject("delivery_partner_id") == null ? null : rs.getLong("delivery_partner_id"), rs.getString("delivery_partner_name"));
					o.setStatus(OrderStatus.valueOf(rs.getString("status")));
					// load items
					o.setItems(loadOrderItems(con, o.getId()));
					return o;
				}
			}
			return null;
		} catch (SQLException e) {
			throw new AppException("Failed to find order: " + e.getMessage(), e);
		}
	}

	@Override
	public List<Order> findAll() {
		List<Order> list = new ArrayList<>();
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(SELECT_ALL);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Order o = new Order(rs.getLong("id"), rs.getLong("customer_id"), rs.getString("customer_name"), new ArrayList<>(), rs.getDouble("total_amount"), rs.getDouble("discount_amount"), rs.getDouble("final_amount"), PaymentMode.valueOf(rs.getString("payment_mode")), rs.getObject("delivery_partner_id") == null ? null : rs.getLong("delivery_partner_id"), rs.getString("delivery_partner_name"));
				o.setStatus(OrderStatus.valueOf(rs.getString("status")));
				o.setItems(loadOrderItems(con, o.getId()));
				list.add(o);
			}
			return list;
		} catch (SQLException e) {
			throw new AppException("Failed to list orders: " + e.getMessage(), e);
		}
	}

	@Override
	public Order update(Order entity) {
		Connection con = null;
		try {
			con = JdbcUtil.getConnection();
			con.setAutoCommit(false);
			try (PreparedStatement ps = con.prepareStatement(UPDATE_SQL)) {
				ps.setLong(1, entity.getCustomerId());
				ps.setString(2, entity.getCustomerName());
				ps.setDouble(3, entity.getTotalAmount());
				ps.setDouble(4, entity.getDiscountAmount());
				ps.setDouble(5, entity.getFinalAmount());
				ps.setString(6, entity.getPaymentMode().name());
				if (entity.getDeliveryPartnerId() == null) ps.setObject(7, null); else ps.setLong(7, entity.getDeliveryPartnerId());
				ps.setString(8, entity.getDeliveryPartnerName());
				ps.setString(9, entity.getStatus().name());
				ps.setLong(10, entity.getId());
				int updated = ps.executeUpdate();
				if (updated <= 0) throw new AppException("Order not found for update: " + entity.getId());
			}

			// delete existing items and re-insert
			try (PreparedStatement dps = con.prepareStatement("DELETE FROM order_items WHERE order_id = ?")) {
				dps.setLong(1, entity.getId());
				dps.executeUpdate();
			}
			try (PreparedStatement ips = con.prepareStatement(INSERT_ORDER_ITEM)) {
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

			con.commit();
			return entity;
		} catch (SQLException e) {
			if (con != null) try { con.rollback(); } catch (SQLException ignore) {}
			throw new AppException("Failed to update order: " + e.getMessage(), e);
		} finally {
			if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignore) {}
		}
	}

	@Override
	public boolean deleteById(Long id) {
		try (Connection con = JdbcUtil.getConnection();
			 PreparedStatement ps = con.prepareStatement(DELETE_SQL)) {
			ps.setLong(1, id);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			throw new AppException("Failed to delete order: " + e.getMessage(), e);
		}
	}
}
