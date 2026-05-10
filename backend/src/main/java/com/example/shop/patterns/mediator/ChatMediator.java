package com.example.shop.patterns.mediator;

import org.java_websocket.WebSocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChatMediator {
    private static ChatMediator INSTANCE;

    private final Map<String, Set<WebSocket>> userConnections  = Collections.synchronizedMap(new HashMap<>());
    private final Map<WebSocket, String>      connectionToUser = Collections.synchronizedMap(new HashMap<>());
    private final Map<WebSocket, String>      connectionToRole = Collections.synchronizedMap(new HashMap<>());

    private ChatMediator() {}

    public static synchronized ChatMediator getInstance() {
        if (INSTANCE == null) INSTANCE = new ChatMediator();
        return INSTANCE;
    }

    public void registerUser(String userId, String role, WebSocket conn) {
        unregisterConnection(conn);
        connectionToUser.put(conn, userId);
        connectionToRole.put(conn, role == null ? "USER" : role.toUpperCase());
        userConnections.computeIfAbsent(userId, k -> Collections.synchronizedSet(new HashSet<>())).add(conn);
        System.out.println("WS auth: conn=" + conn.getRemoteSocketAddress() + " userId=" + userId + " role=" + role);
    }

    public void unregisterConnection(WebSocket conn) {
        String userId = connectionToUser.remove(conn);
        connectionToRole.remove(conn);
        if (userId != null) {
            Set<WebSocket> conns = userConnections.get(userId);
            if (conns != null) {
                conns.remove(conn);
                if (conns.isEmpty()) userConnections.remove(userId);
            }
        }
    }

    public boolean sendToUser(String targetUserId, String message) {
        Set<WebSocket> conns = userConnections.get(targetUserId);
        if (conns == null || conns.isEmpty()) return false;
        synchronized (conns) {
            for (WebSocket c : conns) {
                try { c.send(message); } catch (Exception ignored) {}
            }
        }
        return true;
    }

    public int sendToAdmins(String message) {
        int count = 0;
        synchronized (connectionToRole) {
            for (Map.Entry<WebSocket, String> e : connectionToRole.entrySet()) {
                if ("ADMIN".equals(e.getValue())) {
                    try { e.getKey().send(message); count++; } catch (Exception ignored) {}
                }
            }
        }
        return count;
    }

    public String getUserId(WebSocket conn) { return connectionToUser.get(conn); }
    public String getRole(WebSocket conn)   { return connectionToRole.getOrDefault(conn, "USER"); }

    public void removeFromAllRooms(WebSocket conn) { unregisterConnection(conn); }
}
