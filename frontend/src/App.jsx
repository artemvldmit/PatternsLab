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
import { connect, auth as wsAuth, onMessage, send, isConnected } from "./ws";

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

const EVENT_ICONS = {
  auth: "🔑",
  order: "📦",
  cart: "🛒",
  subscription: "🔔",
  admin: "⚙️",
  chat: "💬",
  message: "📨",
};

function NotificationToast({ notifications }) {
  if (!notifications || notifications.length === 0) return null;
  return (
    <div className="notification-toast-stack">
      {notifications.slice(0, 4).map((n, i) => (
        <div className="notification-toast" key={i}>
          <span className="notification-toast__icon">
            {EVENT_ICONS[n.event] || "ℹ️"}
          </span>
          <span className="notification-toast__text">
            {n.text || n.raw || JSON.stringify(n)}
          </span>
        </div>
      ))}
    </div>
  );
}

const EMPTY_PAYMENT = { cardNumber: "", expiry: "", cvv: "", name: "" };

function PaymentModal({ total, onConfirm, onCancel }) {
  const [form, setForm] = React.useState(EMPTY_PAYMENT);
  const [error, setError] = React.useState("");
  const [loading, setLoading] = React.useState(false);

  function field(key) {
    return (e) => setForm((f) => ({ ...f, [key]: e.target.value }));
  }

  function formatCardNumber(value) {
    return value.replace(/\D/g, "").slice(0, 16).replace(/(.{4})/g, "$1 ").trim();
  }

  function formatExpiry(value) {
    const digits = value.replace(/\D/g, "").slice(0, 4);
    return digits.length > 2 ? digits.slice(0, 2) + "/" + digits.slice(2) : digits;
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const digits = form.cardNumber.replace(/\s/g, "");
    if (digits.length < 16) { setError("Введите корректный номер карты"); return; }
    if (form.expiry.length < 5) { setError("Введите срок действия карты"); return; }
    if (form.cvv.length < 3) { setError("Введите CVV"); return; }
    if (!form.name.trim()) { setError("Введите имя владельца карты"); return; }
    setError("");
    setLoading(true);
    // Simulate payment processing delay
    await new Promise((r) => setTimeout(r, 1200));
    setLoading(false);
    onConfirm();
  }

  return (
    <div className="modal-overlay">
      <div className="modal-card">
        <h3>Оплата заказа</h3>
        <p className="modal-total">Сумма к оплате: <strong>${Number(total).toFixed(2)}</strong></p>
        <form className="payment-form" onSubmit={handleSubmit}>
          <label>
            <span>Номер карты</span>
            <input
              type="text"
              placeholder="0000 0000 0000 0000"
              value={form.cardNumber}
              onChange={(e) => setForm((f) => ({ ...f, cardNumber: formatCardNumber(e.target.value) }))}
              inputMode="numeric"
            />
          </label>
          <div className="payment-form__row">
            <label>
              <span>Срок действия</span>
              <input
                type="text"
                placeholder="ММ/ГГ"
                value={form.expiry}
                onChange={(e) => setForm((f) => ({ ...f, expiry: formatExpiry(e.target.value) }))}
                inputMode="numeric"
              />
            </label>
            <label>
              <span>CVV</span>
              <input
                type="password"
                placeholder="•••"
                maxLength={4}
                value={form.cvv}
                onChange={field("cvv")}
                inputMode="numeric"
              />
            </label>
          </div>
          <label>
            <span>Имя владельца</span>
            <input
              type="text"
              placeholder="IVAN IVANOV"
              value={form.name}
              onChange={field("name")}
            />
          </label>
          {error && <p className="payment-form__error">{error}</p>}
          <div className="modal-footer">
            <button type="button" className="button button--soft" onClick={onCancel} disabled={loading}>
              Отмена
            </button>
            <button type="submit" className="button" disabled={loading}>
              {loading ? "Обработка…" : "Оплатить"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

function AppShell({ role, profile, notifications, children }) {
  const isLoggedIn = profile?.email && profile.email !== "demo@shop.local";

  return (
    <div className="app-shell">
      <div className="ambient ambient--one" />
      <div className="ambient ambient--two" />
      {isLoggedIn && <NotificationToast notifications={notifications} />}

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

function UserDashboard({ products, cart, cartTotal, orderHistory, profile }) {
  return (
    <section className="hero-card">
      <div className="hero-card__grid hero-card__grid--single">
        <div>
          <h2>Добро пожаловать, {profile.name}!</h2>
          <p>
            Перейдите в <strong>Каталог</strong>, чтобы выбрать товары,
            добавьте их в <strong>Корзину</strong> и оформите заказ.
          </p>
          <div className="hero-metrics">
            <div>
              <strong>{products.length}</strong>
              <span>товаров в каталоге</span>
            </div>
            <div>
              <strong>{cart.length}</strong>
              <span>в корзине</span>
            </div>
            <div>
              <strong>${cartTotal.toFixed(2)}</strong>
              <span>сумма корзины</span>
            </div>
            <div>
              <strong>{orderHistory.length}</strong>
              <span>заказов</span>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function AdminDashboard({ products, orderHistory, historyTotal, profile }) {
  return (
    <section className="hero-card">
      <div className="hero-card__grid hero-card__grid--single">
        <div>
          <h2>Панель администратора</h2>
          <p>
            Добро пожаловать, <strong>{profile.name}</strong>.
            Управляйте товарами, ценами и просматривайте аналитику.
          </p>
          <div className="hero-metrics">
            <div>
              <strong>{products.length}</strong>
              <span>товаров</span>
            </div>
            <div>
              <strong>{orderHistory.length}</strong>
              <span>заказов в сессии</span>
            </div>
            <div>
              <strong>${historyTotal.toFixed(2)}</strong>
              <span>оборот сессии</span>
            </div>
          </div>
          <div className="admin-quicklinks">
            <span className="pill pill--muted">Управление → смена цен</span>
            <span className="pill pill--muted">Аналитика → статистика</span>
          </div>
        </div>
      </div>
    </section>
  );
}

function DashboardPage({ products, cart, cartTotal, orderHistory, historyTotal, profile, role }) {
  const isLoggedIn = profile?.email && profile.email !== "demo@shop.local";

  if (!isLoggedIn) {
    return (
      <section className="hero-card">
        <div className="hero-card__grid hero-card__grid--single">
          <div>
            <h2>Добро пожаловать в магазин</h2>
            <p>
              Войдите в аккаунт, чтобы получить доступ к каталогу, корзине и заказам.
            </p>
          </div>
        </div>
      </section>
    );
  }

  if (role === "ADMIN") {
    return <AdminDashboard products={products} orderHistory={orderHistory} historyTotal={historyTotal} profile={profile} />;
  }

  return <UserDashboard products={products} cart={cart} cartTotal={cartTotal} orderHistory={orderHistory} profile={profile} />;
}

function RequireAdmin({ role, children }) {
  if (role !== "ADMIN") {
    return <Navigate to="/dashboard" replace />;
  }
  return children;
}

function LoginPage({ profile, loginForm, setLoginForm, handleLogin, loginError, role }) {
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
            {loginError && (
              <p className="login-error">{loginError}</p>
            )}
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

function CartPage({ cart, cartTotal, refreshCart, placeOrder, paymentModal, setPaymentModal, confirmPayment }) {
  return (
    <section className="dashboard-grid dashboard-grid--single">
      {paymentModal && (
        <PaymentModal
          total={cartTotal}
          onConfirm={confirmPayment}
          onCancel={() => setPaymentModal(false)}
        />
      )}
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
            Оформить и оплатить
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

function ChatThread({ messages, chatInput, setChatInput, onSubmit, placeholder }) {
  return (
    <>
      <div className="chat-window">
        {messages.length === 0 ? (
          <div className="empty-state">{placeholder}</div>
        ) : (
          messages.map((msg, i) => (
            <div
              key={i}
              className={`chat-message${msg.local || msg.role === "ADMIN" ? " chat-message--self" : ""}`}
            >
              <div className="chat-message__meta">
                <strong>{msg.author || "система"}</strong>
                <span>{ROLE_LABELS[msg.role] ?? msg.role ?? "поддержка"}</span>
              </div>
              <p>{msg.text || msg.raw || JSON.stringify(msg)}</p>
            </div>
          ))
        )}
      </div>
      <form className="chat-form" onSubmit={onSubmit}>
        <input
          type="text"
          placeholder="Напишите сообщение..."
          value={chatInput}
          onChange={(e) => setChatInput(e.target.value)}
        />
        <button className="button" type="submit">Отправить</button>
      </form>
    </>
  );
}

function AdminSupportPage({
  chatInput,
  setChatInput,
  sendChatMessage,
  isConnectedValue,
  supportInbox,
  activeSupportUserId,
  setActiveSupportUserId,
}) {
  const [allUsers, setAllUsers] = React.useState([]);

  React.useEffect(() => {
    axios.get("/users").then((r) => {
      setAllUsers(r.data.filter((u) => u.role !== "ADMIN"));
    }).catch(() => {});
  }, []);

  const activeThread = activeSupportUserId
    ? (supportInbox[String(activeSupportUserId)] || { messages: [] })
    : null;

  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Чат с пользователями</h3>
          <span className={isConnectedValue ? "pill" : "pill pill--muted"}>
            {isConnectedValue ? "Онлайн" : "Офлайн"}
          </span>
        </div>

        <div className="support-inbox">
          <div className="support-inbox__sidebar">
            {allUsers.length === 0 ? (
              <div className="empty-state" style={{ padding: "12px" }}>Нет пользователей</div>
            ) : (
              allUsers.map((u) => {
                const uid      = String(u.id);
                const thread   = supportInbox[uid];
                const hasNew   = !!thread;
                const isActive = String(activeSupportUserId) === uid;
                return (
                  <button
                    key={uid}
                    className={`support-inbox__user${isActive ? " support-inbox__user--active" : ""}`}
                    onClick={() => setActiveSupportUserId(uid)}
                  >
                    <strong>{u.email.split("@")[0]}</strong>
                    <span>
                      {hasNew ? `${thread.messages.length} сообщ.` : "нет сообщений"}
                    </span>
                  </button>
                );
              })
            )}
          </div>

          <div className="support-inbox__thread">
            {activeSupportUserId ? (
              <ChatThread
                messages={activeThread.messages}
                chatInput={chatInput}
                setChatInput={setChatInput}
                onSubmit={(e) => sendChatMessage(e, activeSupportUserId)}
                placeholder="Напишите сообщение пользователю..."
              />
            ) : (
              <div className="empty-state">Выберите пользователя из списка</div>
            )}
          </div>
        </div>
      </article>
    </section>
  );
}

function SupportPage({
  role,
  chatMessages,
  chatInput,
  setChatInput,
  sendChatMessage,
  isConnectedValue,
  supportInbox,
  activeSupportUserId,
  setActiveSupportUserId,
}) {
  if (role === "ADMIN") {
    return (
      <AdminSupportPage
        chatInput={chatInput}
        setChatInput={setChatInput}
        sendChatMessage={sendChatMessage}
        isConnectedValue={isConnectedValue}
        supportInbox={supportInbox}
        activeSupportUserId={activeSupportUserId}
        setActiveSupportUserId={setActiveSupportUserId}
      />
    );
  }

  return (
    <section className="dashboard-grid dashboard-grid--single">
      <article className="panel">
        <div className="section-header compact">
          <h3>Чат с поддержкой</h3>
          <span className={isConnectedValue ? "pill" : "pill pill--muted"}>
            {isConnectedValue ? "Онлайн" : "Офлайн"}
          </span>
        </div>
        <ChatThread
          messages={chatMessages}
          chatInput={chatInput}
          setChatInput={setChatInput}
          onSubmit={sendChatMessage}
          placeholder="Напишите сообщение, чтобы начать разговор с поддержкой."
        />
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
  const [paymentModal, setPaymentModal] = useState(false);
  const [loginError, setLoginError] = useState("");
  const [supportInbox, setSupportInbox] = useState({});
  const [activeSupportUserId, setActiveSupportUserId] = useState(null);

  const userIdRef     = React.useRef(userId);
  const roleRef       = React.useRef(role);
  useEffect(() => { userIdRef.current = userId; }, [userId]);
  useEffect(() => { roleRef.current   = role;   }, [role]);

  const isLoggedIn = profile?.email && profile.email !== "demo@shop.local";

  useEffect(() => {
    loadProducts();
    loadCart();

    connect();
    const unsubscribe = onMessage((message) => {

      const parsed = formatEvent(message);
      if (parsed.event === "auth_ok" || parsed.event === "error") return;

      const isSupport = parsed.channel === "support";
      const myId      = String(userIdRef.current);
      const isAdmin   = roleRef.current === "ADMIN";

      if (isSupport) {
        if (isAdmin) {
          // Incoming message FROM a user (no targetUserId = not our own echo)
          if (!parsed.targetUserId) {
            const fromId   = String(parsed.fromUserId || "unknown");
            const fromName = parsed.author || fromId;
            setSupportInbox((prev) => {
              const thread = prev[fromId] || { name: fromName, messages: [] };
              return { ...prev, [fromId]: { ...thread, messages: [...thread.messages, parsed] } };
            });
            setNotifications((cur) => [parsed, ...cur].slice(0, 6));
          }
        } else {
          // Admin reply addressed to this user
          const tid = parsed.targetUserId != null ? String(parsed.targetUserId) : null;
          if (tid === myId) {
            setChatMessages((cur) => [...cur, parsed].slice(-50));
            setNotifications((cur) => [{ event: "chat", text: `Поддержка: ${parsed.text}` }, ...cur].slice(0, 6));
          }
        }
        return;
      }

      // Non-support targeted messages
      if (parsed.targetUserId !== undefined &&
          String(parsed.targetUserId) !== myId) return;

      setNotifications((cur) => [parsed, ...cur].slice(0, 6));
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
    if (!isLoggedInRef.current) return;
    setNotifications((current) => [entry, ...current].slice(0, 6));
  }

  async function handleLogin(event) {
    event.preventDefault();
    setLoginError("");
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
      setRole(user.role);
      setNotifications([]);
      wsAuth(user.id, user.role);
      pushLocalNotice({ event: "auth", text: `Вы вошли как ${user.email}` });
      navigate("/dashboard");
    } catch (err) {
      const data = err?.response?.data;
      if (err?.response?.status === 409 && data?.message) {
        setLoginError(data.message);
      }
    }
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

  function placeOrder() {
    if (cart.length === 0) {
      setStatus("Корзина пуста");
      return;
    }
    setPaymentModal(true);
  }

  async function confirmPayment() {
    setPaymentModal(false);
    await axios.post("/orders/create", { userId });
    const historyEntry = {
      id: Date.now(),
      label: `Заказ #${Date.now().toString().slice(-4)}`,
      total: cartTotal,
      items: [...cart],
    };
    setOrderHistory((current) => [historyEntry, ...current].slice(0, 6));
    setStatus("Заказ оплачен и оформлен");
    await loadCart();
    pushLocalNotice({ event: "order", text: "Заказ оплачен и оформлен" });
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

  function sendChatMessage(event, targetUserId) {
    event.preventDefault();
    const text = chatInput.trim();
    if (!text) return;

    const msgObj = {
      event: "chat",
      channel: "support",
      author: profile.name,
      role,
      fromUserId: String(userId),
      text,
    };
    if (targetUserId) msgObj.targetUserId = String(targetUserId);

    const local = { ...msgObj, local: true };

    if (role === "ADMIN") {
      // Admin reply goes into that user's inbox thread
      if (targetUserId) {
        setSupportInbox((prev) => {
          const thread = prev[String(targetUserId)] || { name: String(targetUserId), messages: [] };
          return {
            ...prev,
            [String(targetUserId)]: { ...thread, messages: [...thread.messages, local] },
          };
        });
      }
    } else {
      setChatMessages((cur) => [...cur, local].slice(-50));
    }

    setChatInput("");
    send(JSON.stringify(msgObj));
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
    loginError,
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
    paymentModal,
    setPaymentModal,
    confirmPayment,
    supportInbox,
    activeSupportUserId,
    setActiveSupportUserId,
  };

  return (
    <AppShell role={role} profile={profile} notifications={notifications}>
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
