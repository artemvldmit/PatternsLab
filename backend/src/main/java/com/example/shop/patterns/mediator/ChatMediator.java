package com.example.shop.patterns.mediator;

import org.java_websocket.WebSocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mediator for chat: rooms mapped to set of WebSocket connections.
 */
public class ChatMediator {
    private static ChatMediator INSTANCE;
    private final Map<String, Set<WebSocket>> rooms = new HashMap<>();

    private ChatMediator() {}

    public static synchronized ChatMediator getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatMediator();
        return INSTANCE;
    }

    public void joinRoom(String roomId, WebSocket conn) {
        rooms.computeIfAbsent(roomId, k -> new HashSet<>()).add(conn);
    }

    public void leaveRoom(String roomId, WebSocket conn) {
        Set<WebSocket> s = rooms.get(roomId);
        if (s != null) s.remove(conn);
    }

    public void removeFromAllRooms(WebSocket conn) {
        for (Set<WebSocket> s : rooms.values()) {
            s.remove(conn);
        }
    }

    public void sendToRoom(String roomId, String message) {
        Set<WebSocket> s = rooms.get(roomId);
        if (s == null) return;
        for (WebSocket c : s) {
            try { c.send(message); } catch (Exception ignored) {}
        }
    }
}
