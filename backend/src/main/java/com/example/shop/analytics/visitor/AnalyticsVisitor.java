package com.example.shop.analytics.visitor;

import com.example.shop.analytics.model.AnalyticsOrder;

public interface AnalyticsVisitor {
    void visit(AnalyticsOrder order);
}
