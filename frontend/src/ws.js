const listeners   = new Set();
let socket         = null;
let lastAuth       = null;   // {userId, role} — re-sent on every reconnect
let messageQueue   = [];     // messages buffered while socket is connecting
let reconnectTimer = null;

function getWsUrl() {
  const host = typeof window !== "undefined" ? window.location.hostname : "localhost";
  return `ws://${host}:18081`;
}

function flushQueue() {
  while (messageQueue.length > 0 && socket && socket.readyState === WebSocket.OPEN) {
    socket.send(messageQueue.shift());
  }
}

function scheduleReconnect() {
  if (reconnectTimer) return;
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null;
    connect();
  }, 2000);
}

export function connect() {
  if (socket && (socket.readyState === WebSocket.OPEN ||
                 socket.readyState === WebSocket.CONNECTING)) {
    return socket;
  }

  socket = new WebSocket(getWsUrl());

  socket.onopen = () => {
    if (lastAuth) {
      socket.send(JSON.stringify({
        event:  "auth",
        userId: String(lastAuth.userId),
        role:   lastAuth.role,
      }));
    }
    flushQueue();
  };

  socket.onmessage = (evt) => {
    listeners.forEach((fn) => fn(evt.data));
  };

  socket.onclose = () => {
    socket = null;
    messageQueue = [];
    if (lastAuth) scheduleReconnect();
  };

  socket.onerror = () => {};

  return socket;
}

export function auth(userId, role) {
  lastAuth = { userId, role: role || "USER" };
  const payload = JSON.stringify({ event: "auth", userId: String(userId), role: role || "USER" });
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(payload);
  } else {
    connect();
  }
}

export function send(message) {
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(message);
  } else {
    messageQueue.push(message);
    connect();
  }
}

export function onMessage(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function isConnected() {
  return !!socket && socket.readyState === WebSocket.OPEN;
}

export default { connect, auth, onMessage, send, isConnected };
