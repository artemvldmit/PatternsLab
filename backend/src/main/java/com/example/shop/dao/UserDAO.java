package com.example.shop.dao;

import com.example.shop.db.DataSourceSingleton;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {

    public Map<String, Object> loginOrCreate(String email, String role) throws SQLException {
        try (Connection c = DataSourceSingleton.getInstance().getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, email, role FROM users WHERE email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return toMap(rs.getInt("id"), rs.getString("email"), rs.getString("role"));
                    }
                }
            }

            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO users (email, role) VALUES (?, ?) RETURNING id, email, role")) {
                ps.setString(1, email);
                ps.setString(2, role);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return toMap(rs.getInt("id"), rs.getString("email"), rs.getString("role"));
                    }
                }
            }
        }
        return null;
    }

    private static Map<String, Object> toMap(int id, String email, String role) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("email", email);
        m.put("role", role);
        return m;
    }
}
