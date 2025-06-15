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
    const shippingCost = shippingFees[shipping] || 0;
    let subtotal = 0;

    items.forEach(i => {
        const el = document.querySelector(`.cart-item[data-product-id="${i.productId}"]`);
        if (el) {
            const priceText = el.querySelector(".price")?.textContent.replace("₱", "") || "0";
            const price = parseFloat(priceText);
            const qty = parseInt(el.querySelector(".quantity-input").value);
            subtotal += price * qty;

        }
    });

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
    const items = JSON.parse(localStorage.getItem("checkoutItems") || "[]");

    const container = document.querySelector(containerSelector);

    if (!container) {
        console.warn(`Container '${containerSelector}' not found.`);
        return;
    }

    container.innerHTML = ""; // Clear existing

    if (!items.length) {
        container.innerHTML = "<p>No items selected for checkout.</p>";
        return;
    }

    fetch("/api/cart")
        .then(res => res.json())
        .then(cartItems => {
            items.forEach(i => {
                const match = cartItems.find(c => c.productId == i.productId);
                if (match) {
                    const html = `
              <div class="cart-item" data-product-id="${match.productId}">
                <img src="${match.imageLoc}" alt="${match.itemName}" class="cartpage-image" />
                <div class="product-details">
                  <p class="cartpage-title">${match.itemName}</p>
                  <div class="price-quantity">
                    <span class="price">₱${match.unitPrice.toFixed(2)}</span>
                    <div class="quantity-controls">
                      <input type="number" value="${i.quantity}" min="1" class="quantity-input" />
                    </div>
                  </div>
                </div>
              </div>
            `;
                    container.insertAdjacentHTML("beforeend", html);
                }
            });

            afterRenderCallback();
            bindQuantityListeners();
        })
        .catch(err => {
            console.error("Failed to load checkout items:", err);
            container.innerHTML = "<p>Failed to load items.</p>";
        });
}
