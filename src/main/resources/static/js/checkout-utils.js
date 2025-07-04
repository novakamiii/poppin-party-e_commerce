export const shippingFees = {
  standard: 45,
  express: 75,
  overnight: 150
};

export function updateSummary() {
  const items = JSON.parse(localStorage.getItem("checkoutItems") || "[]");
  const shipping = localStorage.getItem("shippingOption") || "standard";
  const payment = localStorage.getItem("paymentMethod") || "cod";


  const shippingCost = shippingFees[shipping] || 0;
  let subtotal = items.reduce((sum, item) => {
    const priceText = document.querySelector(`.checkout-cart-item[data-product-id="${item.productId}"] .price`)?.textContent.replace("₱", "") || "0";
    const price = parseFloat(priceText);
    const qty = parseInt(document.querySelector(`.checkout-cart-item[data-product-id="${item.productId}"] .quantity-input`)?.value || item.quantity);
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
      const itemDiv = input.closest(".checkout-cart-item");

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
}

function renderItems(container, items) {
  container.innerHTML = items.map(item => `
    <div class="checkout-cart-item" data-product-id="${item.productId}">
      <img src="${item.imageLoc}" alt="${item.itemName}" class="cartpage-image" />
      <div class="product-details">
        <p class="cartpage-title">${item.itemName}</p>
        <div class="price-quantity">
          <span class="price">₱${item.unitPrice.toFixed(2)}</span>
          <div class="quantity-controls">
            <input type="number" value="${item.quantity}" min="1" class="quantity-input" readonly/>
          </div>
        </div>
      </div>
    </div>
  `).join('');

  updateSummary();
  bindQuantityListeners(); // ✅ Needed to make quantity changes work
}




