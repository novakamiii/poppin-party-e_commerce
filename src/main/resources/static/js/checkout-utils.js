// checkout-utils.js

/**
 * Renders selected checkout items dynamically from localStorage and cart data.
 * Requires the server to expose an /api/cart endpoint.
 *
 * @param {string} containerSelector - The CSS selector for the container element.
 * @param {function} [afterRenderCallback] - Optional function to call after items are rendered.
 * An object representing the shipping fees for different delivery options.
 * @typedef {Object} ShippingFees
 * @property {number} standard - The fee for standard shipping.
 * @property {number} express - The fee for express shipping.
 * @property {number} overnight - The fee for overnight shipping.
 *
 * @type {ShippingFees}
 */
export const shippingFees = {
  standard: 45,
  express: 75,
  overnight: 150
};

export function updateSummary() {
  const items = JSON.parse(localStorage.getItem("checkoutItems") || "[]");
  const shipping = localStorage.getItem("shippingOption") || "standard";
  const payment = localStorage.getItem("paymentMethod") || "paypal";

  const shippingCost = shippingFees[shipping] || 0;
  let subtotal = items.reduce((sum, item) => {
    const priceText = document.querySelector(`.cart-item[data-product-id="${item.productId}"] .price`)?.textContent.replace("₱", "") || "0";
    const price = parseFloat(priceText);
    const qty = parseInt(document.querySelector(`.cart-item[data-product-id="${item.productId}"] .quantity-input`)?.value || item.quantity);
    return sum + (price * qty);
  }, 0);

  const tax = subtotal * 0.12;
  const total = subtotal + shippingCost + tax;

  // Update UI
  const summarySpans = document.querySelectorAll(".summary-item span:last-child");
  if (summarySpans.length >= 4) {
    summarySpans[0].textContent = `₱${subtotal.toFixed(2)}`;
    summarySpans[1].textContent = `₱${shippingCost.toFixed(2)}`;
    summarySpans[2].textContent = `₱${tax.toFixed(2)}`;
    summarySpans[3].textContent = `₱${total.toFixed(2)}`;
  } else {
    console.warn("Missing summary spans in checkout.html");
  }

  // Update hidden form fields
  document.getElementById("shippingOptionInput").value = shipping;
  document.getElementById("paymentMethodInput").value = payment;
  document.getElementById("itemsJsonInput").value = JSON.stringify(items);
}

function bindQuantityListeners() {
  document.querySelectorAll(".quantity-input").forEach(input => {
    input.addEventListener("input", () => {
      const itemDiv = input.closest(".cart-item");
      const productId = itemDiv.getAttribute("data-product-id");
      const quantity = parseInt(input.value);

      const checkoutItems = JSON.parse(localStorage.getItem("checkoutItems") || "[]");
      const itemToUpdate = checkoutItems.find(i => i.productId == productId);

      if (itemToUpdate) {
        itemToUpdate.quantity = quantity;
        localStorage.setItem("checkoutItems", JSON.stringify(checkoutItems));
        updateSummary(); // Update summary when quantity changes
      }
    });
  });
}

export function renderCheckoutItems(containerSelector, afterRenderCallback = () => { }) {
  const container = document.querySelector(containerSelector);
  if (!container) return;

  // First check localStorage for instant display
  const localItems = JSON.parse(localStorage.getItem("checkoutItems") || "[]");

  if (localItems.length > 0) {
    renderItems(container, localItems);
    afterRenderCallback();
  }

  // // Then verify with server
  // fetch("/api/cart")
  //   .then(res => res.json())
  //   .then(serverItems => {
  //     // Map server items to match local format
  //     const formattedItems = serverItems.map(item => ({
  //       productId: item.productId,
  //       quantity: item.quantity,
  //       unitPrice: item.unitPrice,
  //       itemName: item.itemName,
  //       imageLoc: item.imageLoc
  //     }));

  //     if (JSON.stringify(formattedItems) !== JSON.stringify(localItems)) {
  //       localStorage.setItem("checkoutItems", JSON.stringify(formattedItems));
  //       renderItems(container, formattedItems);
  //       afterRenderCallback();
  //     }
  //   })
  //   .catch(console.error);
}

function renderItems(container, items) {
  container.innerHTML = items.map(item => `
    <div class="cart-item" data-product-id="${item.productId}">
      <img src="${item.imageLoc}" alt="${item.itemName}" class="cartpage-image" />
      <div class="product-details">
        <p class="cartpage-title">${item.itemName}</p>
        <div class="price-quantity">
          <span class="price">₱${item.unitPrice.toFixed(2)}</span>
          <div class="quantity-controls">
            <input type="number" value="${item.quantity}" min="1" class="quantity-input" />
          </div>
        </div>
      </div>
    </div>
  `).join('');

  updateSummary();
}

