package com.example.shop.dao;

import com.example.shop.db.DataSourceSingleton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SubscriptionDAO {
    public boolean addSubscription(int userId, int productId) {
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("INSERT INTO subscriptions(user_id, product_id) VALUES(?,?) ON CONFLICT DO NOTHING")) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeSubscription(int userId, int productId) {
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM subscriptions WHERE user_id = ? AND product_id = ?")) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Integer> getSubscribersByProduct(int productId) {
        List<Integer> res = new ArrayList<>();
        try (Connection c = DataSourceSingleton.getInstance().getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT user_id FROM subscriptions WHERE product_id = ?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) res.add(rs.getInt("user_id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }
}
