// checkout-utils.js

/**
 * Renders selected checkout items dynamically from localStorage and cart data.
 * Requires the server to expose an /api/cart endpoint.
 *
 * @param {string} containerSelector - The CSS selector for the container element.
 * @param {function} [afterRenderCallback] - Optional function to call after items are rendered.
 */
// /js/checkout-utils.js

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
  let subtotal = items.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
  const tax = subtotal * 0.12;
  const total = subtotal + shippingCost + tax;

  // Update UI
  const summarySpans = document.querySelectorAll(".summary-item span:last-child");
  if (summarySpans.length >= 4) {
    summarySpans[1].textContent = `₱${shippingCost.toFixed(2)}`;
    summarySpans[2].textContent = `₱${tax.toFixed(2)}`;
    summarySpans[3].textContent = `₱${total.toFixed(2)}`;
  }

  // Update hidden form fields
  document.getElementById("shippingOptionInput").value = shipping;
  document.getElementById("paymentMethodInput").value = payment;
  document.getElementById("itemsJsonInput").value = JSON.stringify(items);
}

// Keep the rest of your checkout-utils.js the same


const tax = subtotal * 0.12;
const total = subtotal + shippingCost + tax;

const summarySpans = document.querySelectorAll(".summary-item span:last-child");
if (summarySpans.length >= 4) {
  summarySpans[0].textContent = `₱${subtotal.toFixed(2)}`;
  summarySpans[1].textContent = `₱${shippingCost.toFixed(2)}`;
  summarySpans[2].textContent = `₱${tax.toFixed(2)}`;
  summarySpans[3].textContent = `₱${total.toFixed(2)}`;
} else {
  console.warn("Missing summary spans in checkout.html");
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

  // Then verify with server
  fetch("/api/cart")
    .then(res => res.json())
    .then(serverItems => {
      if (JSON.stringify(serverItems) !== JSON.stringify(localItems)) {
        localStorage.setItem("checkoutItems", JSON.stringify(serverItems));
        renderItems(container, serverItems);
        afterRenderCallback();
      }
    })
    .catch(console.error);
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

  bindQuantityListeners();
  document.getElementById("itemsJsonInput").value = JSON.stringify(items);
}


