package com.tss.FoodApp.repository.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class BaseJdbcRepository {

    protected void close(ResultSet rs, PreparedStatement ps, Connection con) {
        JdbcUtil.close(rs, ps, con);
    }

    protected PreparedStatement prepareStatement(Connection con, String sql) throws SQLException {
        return con.prepareStatement(sql);
    }

}
