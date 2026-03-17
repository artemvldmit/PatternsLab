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
        mediator.removeFromAllRooms(conn);
        System.out.println("WS close: " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            Map<String, Object> map = gson.fromJson(message, new TypeToken<Map<String, Object>>(){}.getType());
            Object roomId = map.get("roomId");
            Object event = map.get("event");

            if (roomId != null) {
                // auto-join room when we see a roomId
                mediator.joinRoom(String.valueOf(roomId), conn);
            }

            if ("chat".equals(String.valueOf(event)) && roomId != null) {
                mediator.sendToRoom(String.valueOf(roomId), message);
                return;
            }

            // fallback: broadcast to all clients
            conn.send("echo:" + message);
        } catch (Exception ex) {
            try { conn.send("error:invalid message"); } catch (Exception ignored) {}
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

    public void broadcast(String message) {
        synchronized (clients) {
            for (WebSocket c : clients) {
                try { c.send(message); } catch (Exception ignored) {}
            }
        }
    }
}
