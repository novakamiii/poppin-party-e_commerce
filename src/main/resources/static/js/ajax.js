
/**
 * Sends form data (including image) to backend to add a product,
 * then calls a callback with the new product data to update UI.
 *
 * @param {HTMLFormElement} formElement - The form containing product data.
 * @param {function} onSuccess - Callback function(product) called after successful addition.
 * @param {function} onError - Callback function() called on failure.
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
