import React, { useEffect, useMemo, useState } from "react";
import axios from "axios";
import {
  Navigate,
  NavLink,
  Route,
  Routes,
  useNavigate,
} from "react-router-dom";
import ProductList from "./components/ProductList";
import { connect, onMessage, send, isConnected, joinRoom } from "./ws";

const initialPriceForm = { productId: "", price: "" };

function formatEvent(message) {
  if (!message) return { event: "message", text: "Empty message" };
  if (typeof message === "string") {
    try {
      return JSON.parse(message);
    } catch {
      return { event: "message", raw: message };
    }
  }
  return message;
}

const ROLE_LABELS = { USER: "Покупатель", ADMIN: "Администратор" };

function AppShell({ role, profile, children }) {
  const isLoggedIn = profile?.email && profile.email !== "demo@shop.local";

  return (
    <div className="app-shell">
      <div className="ambient ambient--one" />
      <div className="ambient ambient--two" />

      <header className="topbar">
        <div>
          <h1>Интернет-магазин</h1>
        </div>

        <div className="topbar-actions">
          <span className="pill pill--muted">
            {isLoggedIn ? profile.name : ROLE_LABELS[role] ?? role}
          </span>
          <NavLink className="button button--soft nav-button" to="/login">
            {isLoggedIn ? "Аккаунт" : "Войти"}
          </NavLink>
          <NavLink className="button button--soft nav-button" to="/catalog">
            Каталог
          </NavLink>
          <NavLink className="button button--soft nav-button" to="/cart">
            Корзина
          </NavLink>
          <NavLink className="button button--soft nav-button" to="/orders">
            Заказы
          </NavLink>
          {role === "ADMIN" && (
            <>
              <NavLink className="button button--soft nav-button" to="/admin">
                Управление
              </NavLink>
              <NavLink className="button button--soft nav-button" to="/analytics">
                Аналитика
              </NavLink>
            </>
          )}
          <NavLink className="button button--soft nav-button" to="/support">
            Поддержка
          </NavLink>
        </div>
      </header>

      <main className="layout">{children}</main>
    </div>
  );
}

function DashboardPage({ products, cart, cartTotal, profile }) {
  const isLoggedIn = profile?.email && profile.email !== "demo@shop.local";

  return (
    <>
      <section className="hero-card">
        <div className="hero-card__grid hero-card__grid--single">
          <div>
            <h2>
              {isLoggedIn
                ? `Добро пожаловать, ${profile.name}!`
                : "Добро пожаловать в магазин"}
            </h2>
            <p>
              Перейдите в <strong>Каталог</strong>, чтобы выбрать товары,
              добавьте их в <strong>Корзину</strong> и оформите заказ.
            </p>

            <div className="hero-metrics">
              <div>
                <strong>{products.length}</strong>
                <span>товаров</span>
              </div>
              <div>
                <strong>{cart.length}</strong>
                <span>в корзине</span>
              </div>
              <div>
                <strong>${cartTotal.toFixed(2)}</strong>
                <span>итого</span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </>
  );
}

function RequireAdmin({ role, children }) {
  if (role !== "ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

function LoginPage({ profile, loginForm, setLoginForm, handleLogin, role }) {
  const isLoggedIn = profile.email && profile.email !== "demo@shop.local";

  return (
    <section className="hero-card hero-card--wide">
      <div className="hero-card__grid hero-card__grid--single">
        <div>
          <h2>Вход в аккаунт</h2>
          <p>
            Введите email и нажмите <strong>Войти</strong>.
            Если вы впервые — аккаунт создастся автоматически.
          </p>
          {isLoggedIn && (
            <div className="login-summary" style={{ marginTop: "1rem" }}>
              <span className="pill">Вы вошли как:</span>
              <span className="pill pill--muted">{profile.email}</span>
              <span className="pill pill--muted">
                {ROLE_LABELS[role] ?? role}
              </span>
            </div>
          )}
        </div>

        <aside className="hero-card__panel">
          <form className="login-form" onSubmit={handleLogin}>
            <label>
              <span>Email</span>
              <input
                type="email"
                required
                placeholder="your@email.com"
                value={loginForm.email}
                onChange={(event) =>
                  setLoginForm((current) => ({
                    ...current,
                    email: event.target.value,
                  }))
                }
              />
            </label>
            <label>
              <span>Роль</span>
              <select
                value={loginForm.role}
                onChange={(event) =>
                  setLoginForm((current) => ({
                    ...current,
                    role: event.target.value,
                  }))
                }
              >
                <option value="USER">Покупатель</option>
                <option value="ADMIN">Администратор</option>
              </select>
            </label>
            <button className="button" type="submit">
              Войти
            </button>
          </form>
        </aside>
      </div>
    </section>
  );
}

function CatalogPage({
  products,
  subscribeToProduct,
  addToCart,
  quickOrder,
  loadProducts,
}) {
  return (
    <section className="section-block">
      <div className="section-header">
        <div>
          <h2>Все товары</h2>
        </div>
        <button className="button button--soft" onClick={loadProducts}>
          Обновить
        </button>
      </div>

      <ProductList
        products={products}
        onSubscribe={subscribeToProduct}
        onAddToCart={addToCart}
        onQuickOrder={quickOrder}
      />
    </section>
  );
}

function CartPage({ cart, cartTotal, refreshCart, placeOrder }) {
  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Корзина</h3>
          <span className="pill pill--muted">{cart.length} товаров</span>
        </div>

        {cart.length === 0 ? (
          <div className="empty-state">
            Корзина пуста. Добавьте товары из каталога.
          </div>
        ) : (
          <ul className="cart-list">
            {cart.map((item, index) => (
              <li key={`${item.id}-${index}`}>
                <span>{item.name}</span>
                <strong>${Number(item.price || 0).toFixed(2)}</strong>
              </li>
            ))}
          </ul>
        )}

        <div className="panel-footer">
          <span className="panel-note">Итого: ${cartTotal.toFixed(2)}</span>
          <button className="button button--soft" onClick={refreshCart}>
            Обновить
          </button>
          <button
            className="button"
            onClick={placeOrder}
            disabled={cart.length === 0}
          >
            Оформить заказ
          </button>
        </div>
      </article>
    </section>
  );
}

function OrdersPage({ orderHistory, historyTotal, repeatOrder }) {
  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>История заказов</h3>
          <span className="pill pill--muted">${historyTotal.toFixed(2)}</span>
        </div>

        {orderHistory.length === 0 ? (
          <div className="empty-state">
            Заказов пока нет. Добавьте товары в корзину и нажмите «Оформить
            заказ».
          </div>
        ) : (
          <div className="history-list">
            {orderHistory.map((order) => (
              <div className="history-item" key={order.id}>
                <div>
                  <strong>{order.label}</strong>
                  <p>
                    {order.items.length} товаров · $
                    {Number(order.total || 0).toFixed(2)}
                  </p>
                </div>
                <button
                  className="button button--soft"
                  onClick={() => repeatOrder(order)}
                >
                  Повторить заказ
                </button>
              </div>
            ))}
          </div>
        )}
      </article>
    </section>
  );
}

function AdminPage({
  role,
  products,
  priceForm,
  setPriceForm,
  updatePrice,
  selectedProductName,
}) {
  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Изменение цены</h3>
          <span className="pill pill--muted">{ROLE_LABELS[role] ?? role}</span>
        </div>

        <div className="form-grid">
          <label>
            <span>Товар</span>
            <select
              value={priceForm.productId}
              onChange={(event) =>
                setPriceForm((current) => ({
                  ...current,
                  productId: event.target.value,
                }))
              }
            >
              <option value="">Выберите товар</option>
              {products.map((product) => (
                <option value={product.id} key={product.id}>
                  {product.name}
                </option>
              ))}
            </select>
          </label>

          <label>
            <span>Новая цена</span>
            <input
              type="number"
              min="0"
              step="0.01"
              value={priceForm.price}
              onChange={(event) =>
                setPriceForm((current) => ({
                  ...current,
                  price: event.target.value,
                }))
              }
            />
          </label>
        </div>

        <div className="panel-footer">
          <span className="panel-note">Выбрано: {selectedProductName}</span>
          <button className="button" onClick={updatePrice}>
            Обновить цену
          </button>
        </div>
      </article>
    </section>
  );
}

function AnalyticsPage({ role }) {
  const [analytics, setAnalytics] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadAnalytics() {
      try {
        const response = await axios.get(`/analytics?role=${role}`);
        setAnalytics(response.data.report);
        setError("");
      } catch (err) {
        setAnalytics(null);
        setError(err?.response?.data?.message || "Analytics unavailable");
      }
    }

    loadAnalytics();
  }, [role]);

  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Аналитика</h3>
          <span className="pill pill--muted">{ROLE_LABELS[role] ?? role}</span>
        </div>

        {error ? (
          <div className="empty-state">{error}</div>
        ) : analytics ? (
          <div className="analytics-grid">
            <div className="analytics-card">
              <span>Заказов</span>
              <strong>{analytics.orderCount}</strong>
            </div>
            <div className="analytics-card">
              <span>Средний чек</span>
              <strong>${Number(analytics.averageOrderTotal).toFixed(2)}</strong>
            </div>
            <div className="analytics-card analytics-card--wide">
              <span>Популярные товары</span>
              <div className="analytics-tags">
                {analytics.topProducts.map((product) => (
                  <span className="pill pill--muted" key={product}>
                    {product}
                  </span>
                ))}
              </div>
            </div>
            <div className="analytics-card analytics-card--wide">
              <span>Купоны</span>
              <div className="analytics-list">
                {Object.entries(analytics.couponFrequency).map(
                  ([coupon, value]) => (
                    <div className="analytics-list__row" key={coupon}>
                      <strong>{coupon}</strong>
                      <span>{value}</span>
                    </div>
                  ),
                )}
              </div>
            </div>
          </div>
        ) : (
          <div className="empty-state">Загрузка…</div>
        )}
      </article>
    </section>
  );
}

function SupportPage({
  chatMessages,
  chatInput,
  setChatInput,
  sendChatMessage,
  isConnectedValue,
}) {
  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Чат с поддержкой</h3>
          <span className={isConnectedValue ? "pill" : "pill pill--muted"}>
            {isConnectedValue ? "Онлайн" : "Офлайн"}
          </span>
        </div>

        <div className="chat-window">
          {chatMessages.length === 0 ? (
            <div className="empty-state">
              Напишите сообщение, чтобы начать разговор с поддержкой.
            </div>
          ) : (
            chatMessages.map((message, index) => (
              <div
                className="chat-message"
                key={`${message.text || message.raw || index}`}
              >
                <div className="chat-message__meta">
                  <strong>{message.author || "система"}</strong>
                  <span>
                    {ROLE_LABELS[message.role] ??
                      message.role ??
                      message.channel ??
                      "поддержка"}
                  </span>
                </div>
                <p>{message.text || message.raw || JSON.stringify(message)}</p>
              </div>
            ))
          )}
        </div>

        <form className="chat-form" onSubmit={sendChatMessage}>
          <input
            type="text"
            placeholder="Напишите сообщение..."
            value={chatInput}
            onChange={(event) => setChatInput(event.target.value)}
          />
          <button className="button" type="submit">
            Отправить
          </button>
        </form>
      </article>
    </section>
  );
}

export default function App() {
  const navigate = useNavigate();
  const [role, setRole] = useState("USER");
  const [userId, setUserId] = useState(1);
  const [profile, setProfile] = useState({
    email: "demo@shop.local",
    name: "Demo User",
  });
  const [loginForm, setLoginForm] = useState({
    email: "demo@shop.local",
    role: "USER",
  });
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [orderHistory, setOrderHistory] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const [status, setStatus] = useState("Ready");
  const [priceForm, setPriceForm] = useState(initialPriceForm);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState("");

  useEffect(() => {
    loadProducts();
    loadCart();

    connect();
    const unsubscribe = onMessage((message) => {
      const parsed = formatEvent(message);
      setNotifications((current) => [parsed, ...current].slice(0, 6));

      if (parsed.event === "chat" || parsed.channel === "support") {
        setChatMessages((current) => [parsed, ...current].slice(0, 12));
      }
    });

    return () => unsubscribe();
  }, []);

  const cartTotal = useMemo(
    () => cart.reduce((sum, item) => sum + Number(item.price || 0), 0),
    [cart],
  );

  const historyTotal = useMemo(
    () =>
      orderHistory.reduce((sum, order) => sum + Number(order.total || 0), 0),
    [orderHistory],
  );

  async function loadProducts() {
    const response = await axios.get("/products");
    setProducts(response.data);
  }

  async function loadCart() {
    try {
      const response = await axios.get("/cart");
      setCart(response.data);
    } catch {
      setCart([]);
    }
  }

  function pushLocalNotice(entry) {
    setNotifications((current) => [entry, ...current].slice(0, 6));
  }

  async function handleLogin(event) {
    event.preventDefault();
    const email = loginForm.email.trim();
    if (!email) return;

    try {
      const response = await axios.post("/users/login", {
        email,
        role: loginForm.role,
      });
      const user = response.data;
      const nextProfile = {
        email: user.email,
        name: user.email.split("@")[0] || "User",
      };
      setUserId(user.id);
      setProfile(nextProfile);
      setRole(user.role || loginForm.role);
      setStatus(`Logged in as ${nextProfile.name} (${user.role})`);
      pushLocalNotice({
        event: "auth",
        text: `Signed in as ${user.email}`,
      });
    } catch {
      const nextProfile = {
        email,
        name: email.split("@")[0] || "User",
      };
      setProfile(nextProfile);
      setRole(loginForm.role);
      setStatus(`Logged in as ${nextProfile.name} (${loginForm.role})`);
      pushLocalNotice({
        event: "auth",
        text: `Signed in as ${email}`,
      });
    }

    navigate("/dashboard");
  }

  async function subscribeToProduct(productId) {
    await axios.post("/subscriptions/add", { userId, productId });
    setStatus(`Subscribed to product #${productId}`);
    pushLocalNotice({
      event: "subscription",
      productId,
      text: "Subscription added",
    });
  }

  async function addToCart(product) {
    await axios.post("/cart/add", {
      id: product.id,
      name: product.name,
      price: product.price,
      type: product.type || "PHYSICAL",
    });
    setStatus(`${product.name} added to cart`);
    await loadCart();
    pushLocalNotice({
      event: "cart",
      productId: product.id,
      text: `${product.name} added to cart`,
    });
  }

  async function quickOrder(product) {
    await addToCart(product);
    await axios.post("/orders/create", { userId });
    const historyEntry = {
      id: Date.now(),
      label: `Order #${Date.now().toString().slice(-4)}`,
      total: product.price,
      items: [product],
    };
    setOrderHistory((current) => [historyEntry, ...current].slice(0, 6));
    setStatus(`Order created from ${product.name}`);
    await loadCart();
    pushLocalNotice({
      event: "order",
      productId: product.id,
      text: `Order created`,
    });
    navigate("/orders");
  }

  function repeatOrder(order) {
    const [firstItem] = order.items || [];
    if (!firstItem) return;
    addToCart(firstItem);
    setStatus(`Repeated ${order.label}`);
    pushLocalNotice({ event: "history", text: `Repeated ${order.label}` });
  }

  async function placeOrder() {
    if (cart.length === 0) {
      setStatus("Cart is empty");
      return;
    }
    await axios.post("/orders/create", { userId });
    const historyEntry = {
      id: Date.now(),
      label: `Order #${Date.now().toString().slice(-4)}`,
      total: cartTotal,
      items: [...cart],
    };
    setOrderHistory((current) => [historyEntry, ...current].slice(0, 6));
    setStatus("Order placed successfully");
    await loadCart();
    pushLocalNotice({ event: "order", text: "Order placed from cart" });
    navigate("/orders");
  }

  async function updatePrice() {
    if (!priceForm.productId || !priceForm.price) {
      setStatus("Select product and price first");
      return;
    }

    const productId = Number(priceForm.productId);
    const nextPrice = Number(priceForm.price);

    await axios.post("/products/update-price", {
      productId,
      price: nextPrice,
    });
    setStatus(`Price updated for product #${productId}`);
    setPriceForm(initialPriceForm);
    await loadProducts();
    pushLocalNotice({
      event: "admin",
      productId,
      text: "Price update requested",
    });
  }

  function sendChatMessage(event) {
    event.preventDefault();
    const text = chatInput.trim();
    if (!text) return;

    const roomId = "support-demo";
    joinRoom(roomId);

    const payload = JSON.stringify({
      channel: "support",
      event: "chat",
      author: profile.name,
      role,
      text,
      roomId,
    });

    setChatMessages((current) =>
      [
        { event: "chat", author: profile.name, text, role, local: true },
        ...current,
      ].slice(0, 12),
    );
    setChatInput("");
    send(payload);
  }

  const selectedProductName =
    products.find((item) => String(item.id) === String(priceForm.productId))
      ?.name || "Select a product";

  const routeCommonProps = {
    products,
    cart,
    cartTotal,
    orderHistory,
    historyTotal,
    notifications,
    status,
    role,
    profile,
    loginForm,
    setLoginForm,
    handleLogin,
    loadProducts,
    loadCart,
    refreshCart: loadCart,
    subscribeToProduct,
    addToCart,
    quickOrder,
    repeatOrder,
    placeOrder,
    priceForm,
    setPriceForm,
    updatePrice,
    selectedProductName,
    chatMessages,
    chatInput,
    setChatInput,
    sendChatMessage,
    isConnectedValue: isConnected(),
  };

  return (
    <AppShell role={role} profile={profile}>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route
          path="/dashboard"
          element={<DashboardPage {...routeCommonProps} />}
        />
        <Route path="/login" element={<LoginPage {...routeCommonProps} />} />
        <Route
          path="/catalog"
          element={<CatalogPage {...routeCommonProps} />}
        />
        <Route path="/cart" element={<CartPage {...routeCommonProps} />} />
        <Route path="/orders" element={<OrdersPage {...routeCommonProps} />} />
        <Route
          path="/admin"
          element={
            <RequireAdmin role={role}>
              <AdminPage {...routeCommonProps} />
            </RequireAdmin>
          }
        />
        <Route
          path="/analytics"
          element={
            <RequireAdmin role={role}>
              <AnalyticsPage {...routeCommonProps} />
            </RequireAdmin>
          }
        />
        <Route
          path="/support"
          element={<SupportPage {...routeCommonProps} />}
        />
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </AppShell>
  );
}
