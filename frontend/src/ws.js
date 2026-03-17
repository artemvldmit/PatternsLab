const listeners = new Set();
let socket = null;

function getWsUrl() {
  if (typeof window !== "undefined" && window.location.hostname) {
    return `ws://${window.location.hostname}:18081`;
  }
  return "ws://localhost:18081";
}

export function connect() {
  if (socket && socket.readyState === WebSocket.OPEN) {
    return socket;
  }

  if (socket && socket.readyState === WebSocket.CONNECTING) {
    return socket;
  }

  socket = new WebSocket(getWsUrl());
  socket.onmessage = (evt) => {
    listeners.forEach((listener) => listener(evt.data));
  };
  socket.onopen = () => {};
  socket.onclose = () => { socket = null; };
  socket.onerror = () => {};
  return socket;
}

export function onMessage(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}

export function send(message) {
  if (!socket || socket.readyState !== WebSocket.OPEN) {
    connect();
  }
  if (socket && socket.readyState === WebSocket.OPEN) {
    socket.send(message);
  }
}

export function isConnected() {
  return !!socket && socket.readyState === WebSocket.OPEN;
}

export function joinRoom(roomId) {
  if (!roomId) return;
  const payload = JSON.stringify({ action: "join", roomId });
  send(payload);
}

export function leaveRoom(roomId) {
  if (!roomId) return;
  const payload = JSON.stringify({ action: "leave", roomId });
  send(payload);
}

export default { connect, onMessage, send, isConnected };
