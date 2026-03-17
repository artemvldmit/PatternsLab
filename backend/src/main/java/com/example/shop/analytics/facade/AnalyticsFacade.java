package com.example.shop.analytics.facade;

import com.example.shop.analytics.AnalyticsService;
import com.example.shop.analytics.model.AnalyticsOrder;
import com.example.shop.analytics.model.AnalyticsReport;
import com.example.shop.analytics.visitor.AnalyticsCollector;

import java.util.List;

public class AnalyticsFacade implements AnalyticsService {
    @Override
    public AnalyticsReport getAnalytics() {
        List<AnalyticsOrder> orders = List.of(
                new AnalyticsOrder(1, 59.99, "WELCOME10", List.of("Chair", "Premium")),
                new AnalyticsOrder(2, 19.99, "WELCOME10", List.of("E-Book")),
                new AnalyticsOrder(3, 129.50, "VIP15", List.of("Chair", "Premium", "Chair")),
                new AnalyticsOrder(4, 79.00, null, List.of("Chair", "E-Book"))
        );

        AnalyticsCollector collector = new AnalyticsCollector();
        // Visitor double-dispatch: each order calls v.visit(this) via accept()
        orders.forEach(order -> order.accept(collector));

        return new AnalyticsReport(
                collector.getOrderCount(),
                collector.getAverageOrderTotal(),
                collector.getTopProducts(),
                collector.getCouponFrequency()
        );
    }
}
