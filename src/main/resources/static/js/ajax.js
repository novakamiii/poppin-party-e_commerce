


/**
 * Sends form data (including image) to backend to add a product,
 * then calls a callback with the new product data to update UI.
 *
 * @param {HTMLFormElement} formElement - The form containing product data.
 * @param {function} onSuccess - Callback function(product) called after successful addition.
 * @param {function} onError - Callback function() called on failure.
 */
/**
 * Sends an AJAX POST request to add a new product using the provided form element.
 *
 * @param {HTMLFormElement} formElement - The form element containing product data to submit.
 * @param {function(Object): void} onSuccess - Callback function invoked with the created product object on success.
 * @param {function(): void} [onError] - Optional callback function invoked if the request fails.
 */
export function addProductAjax(formElement, onSuccess, onError) {
  const formData = new FormData(formElement);

  fetch('/api/products', {
    method: 'POST',
    body: formData
  })
    .then(response => {
      if (!response.ok) throw new Error('Network response was not OK');
      return response.json();
    })
    .then(product => {
      onSuccess(product);
    })
    .catch(() => {
      if (onError) onError();
    });
}

/**
 * Given a product object, returns an HTML string for the product card.
 *
 * @param {object} product - The product object with id, title, price, imagePath.
 * @returns {string} HTML string of product card.
 */
export function createProductCardHTML(product) {
  return `
      <div class="product-card">
        <div class="product-image">
          <a href="/product-page/${product.id}">
            <img src="${product.imageLoc}" alt="${product.itemName}" />
          </a>
        </div>
        <div class="product-details">
          <a href="/product-page/${product.id}">
            <p class="product-title">${product.itemName}</p>
          </a>
          <p class="product-price">₱${product.price}</p>
        </div>
      </div>
    `;
}

// Initialize cart counter on page load
export function initCartCounter() {
  updateCartCounter();

  // Set up periodic refresh (every 30 seconds)
  setInterval(updateCartCounter, 30000);
}

// Update cart counter via AJAX
export function updateCartCounter() {
  fetch('/api/cart/count')
    .then(res => {
      if (!res.ok) throw new Error('Failed to fetch cart count');
      return res.json();
    })
    .then(count => {
      const counterElement = document.getElementById('cart-counter');
      if (counterElement) {
        counterElement.textContent = count;
        counterElement.style.display = count > 0 ? 'flex' : 'none';
      }
    })
    .catch(err => console.error('Cart counter error:', err));
}

// Call this after successful add to cart
export function handleAddToCartSuccess() {
  updateCartCounter();
  // Optional: Show a temporary visual feedback
  const counter = document.getElementById('cart-counter');
  if (counter) {
    counter.classList.add('pulse');
    setTimeout(() => counter.classList.remove('pulse'), 500);
  }
}


export async function handleBuyNow(productId, quantity) {
  try {
    const response = await fetch(`/order/buy-now?productId=${productId}&quantity=${quantity}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'application/json'
      },
      credentials: 'include'
    });

    // Handle authentication redirects
    if (response.redirected && response.url.includes('/login')) {
      alert('You must be logged in to access this feature!');
      window.location.href = response.url;
      return;
    }

    // Check for HTML response (login page)
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('text/html')) {
      window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
      return;
    }

    // Handle JSON responses
    if (!contentType || !contentType.includes('application/json')) {
      const text = await response.text();
      throw new Error(text || 'Invalid response from server');
    }

    const data = await response.json();

    if (!response.ok) {
      // Handle specific authentication errors
      if (response.status === 401 || response.status === 403) {
        window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
        return;
      }
      throw new Error(data.error || 'Purchase failed');
    }

    // Success case
    updateCartCounter(1);
    localStorage.setItem("checkoutItems", JSON.stringify([data]));
    window.location.href = "/order/checkout";

  } catch (error) {
    console.error('Buy Now error:', error);
    if (!error.message.includes('JSON')) { // Don't show parse errors to user
      alert('Please log in to complete your purchase');
    }
    window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
  }
}




