document.addEventListener("DOMContentLoaded", () => {
  const cartContainer = document.getElementById("cartContainer");
  const cartTotal = document.getElementById("cartTotal");
  const checkoutButton = document.querySelector(".checkout-button");


  function showEmptyCart() {
    cartContainer.innerHTML = `
      <div class="empty-cart">
        <img src="/img/empty-cart.png" alt="Empty cart" class="empty-cart-image">
        <h3 class="empty-cart-title">Your cart is empty</h3>
        <p class="empty-cart-message">Looks like you haven't added any items yet</p>
        <a href="/products/see-more" class="empty-cart-button">Browse Products</a>
      </div>
    `;
    cartTotal.textContent = "0.00";
    checkoutButton.style.display = "none";
  }



  function renderCartItem(item) {
    if (!item.imageLoc || item.productId === "-1" || item.productId === null || item.productId === undefined) {
      return `
  <div class="cart-item sold-out" data-order-item-id="${item.id}" data-product-id="${item.productId}" data-disabled="true">
    <img src="https://placehold.co/150x100?text=Sold+Out" alt="Sold Out" class="cartpage-image" />
    <div class="product-details">
      <p class="cartpage-title">${item.itemName || "Product Unavailable"}</p>
      <div class="price-quantity">
        <span class="price">₱0.00</span>
        <span class="sold-out-label">Restock / Sold Out</span>
      </div>
    </div>
    <div class="actions">
      <input class="action-btn item-check" type="checkbox"/>
      <button class="action-btn remove-btn" tabindex="0"><img src="/img/x.png" alt="remove"></button>
    </div>
  </div>
  `;
    }

    // Normal product
    return `
     <div class="cart-item" data-order-item-id="${item.id}" data-product-id="${item.productId}">
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
      const checkbox = item.querySelector(".item-check");
      const priceElem = item.querySelector(".price");
      const qtyElem = item.querySelector(".quantity-input");

      if (!checkbox || !priceElem || !qtyElem) return; // skip sold out or broken items

      if (checkbox.checked) {
        const unitPrice = parseFloat(priceElem.textContent.replace("₱", "")) || 0;
        const qty = parseInt(qtyElem.value) || 1;
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


  const clearCartButton = document.getElementById("clearCartButton");

  clearCartButton.addEventListener("click", () => {
    if (!confirm("Are you sure you want to clear all items from your cart?")) return;

    fetch("/api/cart/clear", {
      method: "POST"
    })
      .then(res => {
        if (!res.ok) throw new Error("Failed to clear cart");
        showEmptyCart();
      })
      .catch(err => {
        console.error("Error clearing cart:", err);
        alert("Something went wrong while clearing the cart.");
      });
  });


  function removeItem(productId, cartItemElement) {
    const params = new URLSearchParams();
    params.append("id", productId); // if productId is actually OrderItem.id
    console.log("🟦 removeItem() called with:", productId);

    fetch("/api/cart/remove", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: params.toString()
    }).then(res => {
      if (!res.ok) throw new Error("Failed to remove item");
      if (cartItemElement) cartItemElement.remove();
      updateTotal();

      if (document.querySelectorAll(".cart-item").length === 0) {
        showEmptyCart();
      }
    }).catch(err => {
      alert("Error removing item.");
      console.error("Remove item error:", err);
      if (err && err.stack) {
        console.error(err.stack);
      }
    });
  }


  fetch("/api/cart")
    .then(res => {
      if (!res.ok) throw new Error("Failed to fetch cart");
      return res.json();
    })
    .then(items => {
      if (items.length === 0) {
        showEmptyCart();
        return;
      }

      items.forEach(item => {
        cartContainer.insertAdjacentHTML("beforeend", renderCartItem(item));
      });

      updateTotal();

      cartContainer.addEventListener("click", e => {
        const cartItem = e.target.closest(".cart-item");
        if (!cartItem) return;
        const productId = cartItem.getAttribute("data-product-id");
        const input = cartItem.querySelector(".quantity-input");
        if (input) {
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
        }


        if (e.target.closest(".remove-btn")) {
          if (confirm("Remove this item from your cart?")) {
            const orderItemId = cartItem.getAttribute("data-order-item-id");
            removeItem(orderItemId, cartItem);
          }
        }

      });

      cartContainer.addEventListener("input", e => {
        if (!e.target.classList.contains("quantity-input")) return;

        const cartItem = e.target.closest(".cart-item");
        if (!cartItem) return;

        const productId = cartItem.getAttribute("data-order-item-id");
        const newQty = parseInt(e.target.value);

        if (!isNaN(newQty) && newQty > 0) {
          updateQuantity(productId, newQty);
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
      showEmptyCart();
    });

  checkoutButton.addEventListener("click", () => {
    const selectedItems = [];
    const invalidItems = [];

    document.querySelectorAll(".cart-item").forEach(item => {
      const checkbox = item.querySelector(".item-check");
      const itemName = item.querySelector(".cartpage-title")?.textContent || "Unknown Item";

      if (!checkbox || !checkbox.checked) return;

      const isDisabled = item.dataset.disabled === "true";

      if (isDisabled) {
        invalidItems.push(itemName);
        item.classList.add("highlight-invalid");
      } else {
        selectedItems.push({
          productId: item.getAttribute("data-product-id"),
          quantity: parseInt(item.querySelector(".quantity-input").value),
          unitPrice: parseFloat(item.querySelector(".price").textContent.replace("₱", "")),
          itemName,
          imageLoc: item.querySelector(".cartpage-image").src
        });
      }
    });

    if (invalidItems.length > 0) {
      const itemList = invalidItems.map(name => `- ${name}`).join('\n');
      alert(
        `The following item(s) are unavailable or sold out and cannot be checked out:\n\n${itemList}\n\nPlease uncheck or remove them before proceeding.`
      );
      return window.location.href = "/cart";
    }

    if (selectedItems.length === 0) {
      alert("Please select at least one valid item to proceed to checkout.");
      return window.location.href = "/cart";
    }

    localStorage.setItem("checkoutItems", JSON.stringify(selectedItems));
    window.location.href = "/order/checkout";
  });


});
