package com.example.shop.patterns.observer;

import com.example.shop.ws.ShopWebSocketServer;

/**
 * Simple Observer/Notifier: uses WebSocket server to broadcast order/product events.
 */
public class OrderEventNotifier {
    public static void notifyAllClients(String eventJson) {
        ShopWebSocketServer ws = ShopWebSocketServer.getInstance();
        if (ws != null) ws.broadcast(eventJson);
    }
}
