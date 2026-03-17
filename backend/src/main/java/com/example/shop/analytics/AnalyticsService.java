package com.example.shop.analytics;

import com.example.shop.analytics.model.AnalyticsReport;

/**
 * Subject interface shared by AnalyticsFacade (RealSubject) and AnalyticsProxy.
 * Clients depend on this interface, making Proxy transparent to them.
 */
public interface AnalyticsService {
    AnalyticsReport getAnalytics();
}
