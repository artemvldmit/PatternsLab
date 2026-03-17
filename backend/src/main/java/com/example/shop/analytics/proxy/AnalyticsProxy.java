package com.example.shop.analytics.proxy;

import com.example.shop.analytics.AnalyticsService;
import com.example.shop.analytics.facade.AnalyticsFacade;
import com.example.shop.analytics.model.AnalyticsReport;

/**
 * Protection Proxy: controls access to AnalyticsService for non-ADMIN roles.
 * Implements the same AnalyticsService interface as AnalyticsFacade (RealSubject),
 * so the client cannot distinguish proxy from the real object.
 */
public class AnalyticsProxy implements AnalyticsService {
    private final AnalyticsService delegate = new AnalyticsFacade();
    private final String role;
    private AnalyticsReport cachedReport;

    public AnalyticsProxy(String role) {
        this.role = role;
    }

    @Override
    public AnalyticsReport getAnalytics() {
        if (!"ADMIN".equalsIgnoreCase(role)) {
            throw new SecurityException("Analytics is available for ADMIN only");
        }
        if (cachedReport == null) {
            cachedReport = delegate.getAnalytics();
        }
        return cachedReport;
    }
}
