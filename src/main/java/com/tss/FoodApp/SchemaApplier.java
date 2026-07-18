package com.tss.FoodApp;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

import com.tss.FoodApp.config.DatabaseConfig;

public class SchemaApplier {

    public static void main(String[] args) {
        try (Connection con = DatabaseConfig.getConnection(); Statement st = con.createStatement()) {
            System.out.println("Connected to DB: " + con.getMetaData().getURL());

            String[] drops = new String[]{
                    "DROP TABLE IF EXISTS order_items CASCADE",
                    "DROP TABLE IF EXISTS orders CASCADE",
                    "DROP TABLE IF EXISTS carts CASCADE",
                    "DROP TABLE IF EXISTS menu_items CASCADE",
                    "DROP TABLE IF EXISTS delivery_partners CASCADE",
                    "DROP TABLE IF EXISTS customers CASCADE",
                    "DROP TABLE IF EXISTS admins CASCADE"
            };
            for (String d : drops) {
                System.out.println("Executing: " + d);
                st.execute(d);
            }

            String sqlPath = "sql/create_tables.sql";
            String content = new String(Files.readAllBytes(Paths.get(sqlPath)), StandardCharsets.UTF_8);
            String[] statements = content.split(";\n");
            for (String s : statements) {
                String stmt = s.trim();
                if (stmt.isEmpty()) continue;
                System.out.println("Applying statement chunk...");
                st.execute(stmt);
            }

            System.out.println("Schema applied successfully.");
        } catch (Exception e) {
            System.out.println("Schema application failed: " + e.getMessage());
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}
