package com.example.shop.dao;

import com.example.shop.db.DataSourceSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductDAO {
    public Double getPriceById(int productId) {
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT price FROM products WHERE id = ?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePrice(int productId, double newPrice) {
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE products SET price = ? WHERE id = ?")) {
            ps.setDouble(1, newPrice);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
