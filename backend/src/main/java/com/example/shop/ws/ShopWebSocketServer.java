package com.example.shop.ws;

import com.example.shop.patterns.mediator.ChatMediator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShopWebSocketServer extends WebSocketServer {
    private final Set<WebSocket> clients = Collections.synchronizedSet(new HashSet<>());
    private static ShopWebSocketServer INSTANCE;
    private final ChatMediator mediator = ChatMediator.getInstance();
    private final Gson gson = new Gson();

    public ShopWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        INSTANCE = this;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        clients.add(conn);
        System.out.println("WS open: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        clients.remove(conn);
        mediator.unregisterConnection(conn);
        System.out.println("WS close: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            Map<String, Object> map = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());
            String event = map.containsKey("event") ? String.valueOf(map.get("event")) : "";

            if ("auth".equals(event)) {
                Object uid  = map.get("userId");
                Object role = map.get("role");
                if (uid == null) {
                    conn.send("{\"event\":\"error\",\"message\":\"userId required for auth\"}");
                    return;
                }
                mediator.registerUser(String.valueOf(uid), role == null ? "USER" : String.valueOf(role), conn);
                conn.send("{\"event\":\"auth_ok\",\"userId\":\"" + uid + "\"}");
                return;
            }

            if ("chat".equals(event)) {
                String senderId   = mediator.getUserId(conn);
                String senderRole = mediator.getRole(conn);
                if (senderId == null) {
                    conn.send("{\"event\":\"error\",\"message\":\"not authenticated\"}");
                    return;
                }

                Object targetUid = map.get("targetUserId");
                String channel   = map.containsKey("channel") ? String.valueOf(map.get("channel")) : "";

                if ("support".equals(channel)) {
                    if ("ADMIN".equals(senderRole)) {
                        if (targetUid != null) {
                            mediator.sendToUser(String.valueOf(targetUid), message);
                        } else {
                            conn.send("{\"event\":\"error\",\"message\":\"targetUserId required for admin reply\"}");
                        }
                    } else {
                        mediator.sendToAdmins(message);
                    }
                } else {
                    if (targetUid != null) {
                        boolean delivered = mediator.sendToUser(String.valueOf(targetUid), message);
                        if (!delivered) {
                            conn.send("{\"event\":\"error\",\"message\":\"user not connected\"}");
                        }
                    } else {
                        mediator.sendToUser(senderId, message);
                    }
                }
                return;
            }

            conn.send("{\"event\":\"error\",\"message\":\"unknown event\"}");
        } catch (Exception ex) {
            try { conn.send("{\"event\":\"error\",\"message\":\"invalid message\"}"); } catch (Exception ignored) {}
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        ex.printStackTrace();
    }

    @Override
    public void onStart() {
        System.out.println("WebSocket server started");
    }

    public static ShopWebSocketServer getInstance() {
        return INSTANCE;
    }

    public void sendToUser(String userId, String message) {
        mediator.sendToUser(userId, message);
    }

    public void broadcast(String message) {
        synchronized (clients) {
            for (WebSocket c : clients) {
                try { c.send(message); } catch (Exception ignored) {}
            }
        }
    }
}
