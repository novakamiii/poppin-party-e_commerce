document.addEventListener("DOMContentLoaded", () => {
  const cartContainer = document.getElementById("cartContainer");
  const cartTotal = document.getElementById("cartTotal");

  function renderCartItem(item) {
    const isCustom = item.custom;
    const baseAttributes = `data-product-id="${item.productId}"`;

    const customAttributes = isCustom
      ? `data-size="${item.customSize}"
       data-event-type="${item.eventType}"
       data-message="${item.personalizedMessage}"
       data-thickness="${item.tarpaulinThickness}"
       data-finish="${item.tarpaulinFinish}"`
      : "";

    return `
    <div class="cart-item" ${baseAttributes} ${customAttributes}>
      <img src="${item.imageLoc}" alt="${item.itemName}" class="cartpage-image" />
      <div class="product-details">
        <p class="cartpage-title">${item.itemName}</p>
        <div class="price-quantity">
          <span class="price">₱${item.unitPrice.toFixed(2)}</span>
          <div class="quantity-controls">
            <button class="quantity-btn minus">-</button>
            <input type="number" value="${item.quantity}" min="1" class="quantity-input"/>
            <button class="quantity-btn plus">+</button>
          </div>
        </div>
      </div>
      <div class="actions">
        <input class="action-btn item-check" type="checkbox" checked />
        <button class="action-btn remove-btn"><img src="/img/x.png" alt="remove"></button>
      </div>
    </div>
  `;
  }


  function updateTotal() {
    let total = 0;
    document.querySelectorAll(".cart-item").forEach(item => {
      const isChecked = item.querySelector(".item-check").checked;
      const unitPrice = parseFloat(item.querySelector(".price").textContent.replace("₱", ""));
      const qty = parseInt(item.querySelector(".quantity-input").value);
      if (isChecked) {
        total += unitPrice * qty;
      }
    });
    cartTotal.textContent = total.toFixed(2);
  }

  function updateQuantity(productId, newQty) {
    fetch("/api/cart/update", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `productId=${productId}&quantity=${newQty}`
    }).then(res => {
      if (!res.ok) throw new Error("Failed to update quantity");
      updateTotal();
    }).catch(err => {
      alert("Error updating quantity.");
      console.error(err);
    });
  }

  function removeItem(productId, cartItemElement) {
    let body;

    if (productId === "-1" || productId === -1) {
      // Custom tarpaulin removal – gather custom fields from DOM attributes
      const customSize = cartItemElement.dataset.size;
      const eventType = cartItemElement.dataset.eventType;
      const message = cartItemElement.dataset.message;
      const thickness = cartItemElement.dataset.thickness;
      const finish = cartItemElement.dataset.finish;

      body = new URLSearchParams({
        productId,
        customSize,
        eventType,
        message,
        thickness,
        finish
      });
    } else {
      // Normal product removal
      body = new URLSearchParams({ productId });
    }

    fetch("/api/cart/remove", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: body.toString()
    }).then(res => {
      if (!res.ok) throw new Error("Failed to remove item");
      cartItemElement.remove();
      updateTotal();
    }).catch(err => {
      alert("Error removing item.");
      console.error(err);
    });
  }


  fetch("/api/cart")
    .then(res => {
      if (!res.ok) throw new Error("Failed to fetch cart");
      return res.json();
    })
    .then(items => {
      items.forEach(item => {
        cartContainer.insertAdjacentHTML("beforeend", renderCartItem(item));
      });

      updateTotal();

      cartContainer.addEventListener("click", e => {
        const cartItem = e.target.closest(".cart-item");
        if (!cartItem) return;
        const productId = cartItem.getAttribute("data-product-id");
        const input = cartItem.querySelector(".quantity-input");
        let qty = parseInt(input.value);

        if (e.target.classList.contains("plus")) {
          input.value = ++qty;
          updateQuantity(productId, qty);
        }

        if (e.target.classList.contains("minus")) {
          if (qty > 1) {
            input.value = --qty;
            updateQuantity(productId, qty);
          }
        }

        if (e.target.closest(".remove-btn")) {
          if (confirm("Remove this item from your cart?")) {
            removeItem(productId, cartItem);
          }
        }
      });

      cartContainer.addEventListener("input", e => {
        const cartItem = e.target.closest(".cart-item");
        if (e.target.classList.contains("quantity-input")) {
          const productId = cartItem.getAttribute("data-product-id");
          const newQty = parseInt(e.target.value);
          if (newQty > 0) updateQuantity(productId, newQty);
        }
        updateTotal();
      });

      cartContainer.addEventListener("change", e => {
        if (e.target.classList.contains("item-check")) {
          updateTotal();
        }
      });
    })
    .catch(err => {
      console.error("Error loading cart:", err);
      cartContainer.innerHTML = "<p>Failed to load cart.</p>";
    });

  document.querySelector(".checkout-button").addEventListener("click", () => {
    const selectedItems = [];
    document.querySelectorAll(".cart-item").forEach(item => {
      if (item.querySelector(".item-check").checked) {
        selectedItems.push({
          productId: item.getAttribute("data-product-id"),
          quantity: parseInt(item.querySelector(".quantity-input").value),
          unitPrice: parseFloat(item.querySelector(".price").textContent.replace("₱", "")),
          itemName: item.querySelector(".cartpage-title").textContent
        });

      }
    });

    localStorage.setItem("checkoutItems", JSON.stringify(selectedItems));
    window.location.href = "/order/checkout";
  });
});
