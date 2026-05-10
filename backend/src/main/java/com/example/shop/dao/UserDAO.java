package com.example.shop.dao;

import com.example.shop.db.DataSourceSingleton;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {

    /**
     * Returns the user map on success.
     * Returns a map with key "error" = "role_mismatch" if the account exists under a different role.
     */
    public Map<String, Object> loginOrCreate(String email, String role) throws SQLException {
        try (Connection c = DataSourceSingleton.getInstance().getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, email, role FROM users WHERE email = ?")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String storedRole = rs.getString("role");
                        if (!storedRole.equalsIgnoreCase(role)) {
                            Map<String, Object> err = new HashMap<>();
                            err.put("error", "role_mismatch");
                            err.put("message", "Этот аккаунт уже зарегистрирован как " + storedRole);
                            err.put("existingRole", storedRole);
                            return err;
                        }
                        return toMap(rs.getInt("id"), rs.getString("email"), storedRole);
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

    public List<Map<String, Object>> getAllUsers() throws SQLException {
        List<Map<String, Object>> users = new ArrayList<>();
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT id, email, role FROM users ORDER BY id")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(toMap(rs.getInt("id"), rs.getString("email"), rs.getString("role")));
                }
            }
        }
        return users;
    }

    private static Map<String, Object> toMap(int id, String email, String role) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("email", email);
        m.put("role", role);
        return m;
    }
}
