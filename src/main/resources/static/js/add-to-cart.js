  /**
   * Displays the empty cart UI in the cart container.
   * Updates the cart container with a message and image indicating the cart is empty,
   * resets the cart total to "0.00", and hides the checkout button.
   *
   * Assumes the existence of the following global variables:
   * - cartContainer: The DOM element where the cart contents are displayed.
   * - cartTotal: The DOM element displaying the cart's total price.
   * - checkoutButton: The DOM element for the checkout button.
   */
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
    return `
      <div class="cart-item" data-product-id="${item.productId}">
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
    fetch("/api/cart/remove", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: `productId=${productId}`
    }).then(res => {
      if (!res.ok) throw new Error("Failed to remove item");
      cartItemElement.remove();
      updateTotal();

      // Check if cart is now empty
      if (document.querySelectorAll(".cart-item").length === 0) {
        showEmptyCart();
      }
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
      showEmptyCart();
    });

  checkoutButton.addEventListener("click", () => {
    const selectedItems = [];
    document.querySelectorAll(".cart-item").forEach(item => {
      if (item.querySelector(".item-check").checked) {
        selectedItems.push({
          productId: item.getAttribute("data-product-id"),
          quantity: parseInt(item.querySelector(".quantity-input").value),
          unitPrice: parseFloat(item.querySelector(".price").textContent.replace("₱", "")),
          itemName: item.querySelector(".cartpage-title").textContent,
          imageLoc: item.querySelector(".cartpage-image").src
        });
      }
    });

    if (selectedItems.length === 0) {
      alert("Please select at least one item to proceed to checkout.");
      return;
    }

    localStorage.setItem("checkoutItems", JSON.stringify(selectedItems));
    window.location.href = "/order/checkout";
  });
});
