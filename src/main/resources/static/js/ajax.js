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


//Old version with button inside image link
/*
<a href="/product-page/${product.id}">
            <img src="${product.imageLoc}" alt="${product.itemName}" />
          </a> */

export function createProductCardHTML(product) {
  const outOfStock = product.stock === 0;
  return `
    <a href="/product-page/${product.id}" class="product-card-link" style="text-decoration:none;color:inherit;">
      <div class="product-card">
        <div class="product-image">
          <img src="${product.imageLoc}" alt="${product.itemName}" />
        </div>
        <div class="product-details">
          <p class="product-title">
            ${product.itemName}
            ${outOfStock ? '<span class="out-of-stock-label" style="color:grey;font-weight:bold;margin-left:8px;">(Out of Stock)</span>' : ''}
          </p>
          <p class="product-price">₱${product.price}</p>
        </div>
      </div>
    </a>
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
  if (!Number.isInteger(quantity) || quantity <= 0) {
    alert('Add to cart failed: Please enter a valid quantity!');
    return;
  }

  try {
    const response = await fetch(`/order/buy-now`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        'Accept': 'application/json'
      },
      body: `productId=${productId}&quantity=${quantity}`,
      credentials: 'include'
    });

    const contentType = response.headers.get('content-type');

    // Check for authentication errors or unexpected HTML response
    if (
      response.status === 401 ||
      response.status === 403 ||
      (contentType && contentType.includes('text/html'))
    ) {
      alert('You must be logged in to purchase items!');
      window.location.href = '/login?redirect=' + encodeURIComponent(window.location.pathname);
      return;
    }

    let data = {};
    let fallbackText = null;
    try {
      // Try to parse JSON if possible
      if (contentType && contentType.includes('application/json')) {
        data = await response.json();
      } else {
        // If not JSON, treat as text (could be an error message)
        fallbackText = await response.text();
        throw new Error(fallbackText || 'Unknown error occurred');
      }
    } catch (jsonErr) {
      // If JSON parsing fails, use the fallbackText if available
      if (fallbackText !== null) {
        console.warn('Non-JSON response received:', fallbackText);
        throw new Error(fallbackText || 'Unknown error occurred');
      } else {
        console.warn('Non-JSON response received and no fallbackText available');
        throw new Error('Unknown error occurred');
      }
    }

    if (!response.ok) {
      throw new Error(data.error || data.message || 'Purchase failed');
    }

    updateCartCounter(1);
    localStorage.setItem("checkoutItems", JSON.stringify([data]));
    window.location.href = "/order/checkout";

  } catch (error) {
    console.error('Buy Now error:', error);

    if (
      error.message.toLowerCase().includes('stock') ||
      error.message.toLowerCase().includes('quantity')
    ) {
      alert(error.message);
    } else {
      alert(error.message || 'Something went wrong. Please try again.');
    }
  }
}






