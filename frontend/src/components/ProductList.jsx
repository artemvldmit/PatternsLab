import React from "react";

const TYPE_LABELS = {
  PHYSICAL: "Физический товар",
  DIGITAL: "Цифровой товар",
  SUBSCRIPTION: "Подписка",
};

export default function ProductList({
  products,
  onSubscribe,
  onAddToCart,
  onQuickOrder,
}) {
  return (
    <div className="catalog-grid">
      {products.map((product) => (
        <article className="product-card" key={product.id}>
          <div className="product-card__top">
            <span className="product-badge">
              {TYPE_LABELS[product.type] || product.type || "Товар"}
            </span>
          </div>

          <h3>{product.name}</h3>

          <div className="product-price-row">
            <strong>${Number(product.price).toFixed(2)}</strong>
            <span>за единицу</span>
          </div>

          <div className="product-actions">
            {onSubscribe ? (
              <button
                className="button button--soft"
                onClick={() => onSubscribe(product.id)}
              >
                Подписаться
              </button>
            ) : null}
            {onAddToCart ? (
              <button
                className="button button--soft"
                onClick={() => onAddToCart(product)}
              >
                В корзину
              </button>
            ) : null}
            {onQuickOrder ? (
              <button className="button" onClick={() => onQuickOrder(product)}>
                Купить сейчас
              </button>
            ) : null}
          </div>
        </article>
      ))}
    </div>
  );
}
