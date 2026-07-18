package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.tss.FoodApp.config.DatabaseConfig;

public final class JdbcUtil {

    private JdbcUtil() {
    }

    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public static void close(ResultSet rs,
                             PreparedStatement ps,
                             Connection con) {

        try {
            if (rs != null)
                rs.close();
        } catch (SQLException ignored) {
        }

        try {
            if (ps != null)
                ps.close();
        } catch (SQLException ignored) {
        }

        try {
            if (con != null)
                con.close();
        } catch (SQLException ignored) {
        }
    }
}