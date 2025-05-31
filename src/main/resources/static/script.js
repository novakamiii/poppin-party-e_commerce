// Get references to the elements (including the new span)
const openPaymentPopup = document.getElementById('openPaymentPopup');
const paymentPopup = document.getElementById('paymentPopup');
const closePaymentPopupButton = document.getElementById('closePaymentPopup');
const selectPaymentButton = document.getElementById('selectPaymentButton');
const paymentOptions = document.querySelectorAll('input[name="payment"]');
const selectedPaymentDisplay = document.getElementById('selectedPayment'); // Get the span

// Function to show the popup (remains the same)
function showPaymentPopup() {
  paymentPopup.style.display = 'flex';
}

// Function to hide the popup (remains the same)
function hidePaymentPopup() {
  paymentPopup.style.display = 'none';
}

// Event listener to open the popup (remains the same)
openPaymentPopup.addEventListener('click', showPaymentPopup);

// Event listener to close the popup when the close button is clicked (remains the same)
if (closePaymentPopupButton) {
    closePaymentPopupButton.addEventListener('click', hidePaymentPopup);
  }

// Event listener to close the popup when clicking outside the content (remains the same)
window.addEventListener('click', (event) => {
  if (event.target === paymentPopup) {
    hidePaymentPopup();
  }
});

// Event listener for the "Select" button (modified to update the display)
selectPaymentButton.addEventListener('click', () => {
  let selectedPaymentValue;
  let selectedPaymentLabel;
  paymentOptions.forEach(option => {
    if (option.checked) {
      selectedPaymentValue = option.value;
      selectedPaymentLabel = document.querySelector(`label[for="${option.id}"]`).textContent;
    }
  });

  if (selectedPaymentValue) {
    selectedPaymentDisplay.textContent = selectedPaymentLabel; // Update the text
    hidePaymentPopup();
  } else {
    alert('Please select a payment method.');
  }
});

// Initially set the displayed payment method (optional - if you have a default)
// const initialChecked = document.querySelector('input[name="payment"]:checked');
// if (initialChecked) {
//   selectedPaymentDisplay.textContent = document.querySelector(`label[for="${initialChecked.id}"]`).textContent;
// }




// Get references to the elements
const openShippingPopup = document.getElementById('openShippingPopup');
const shippingPopup = document.getElementById('shippingPopup');
const closeShippingPopupButton = document.getElementById('closeShippingPopup');
const selectShippingButton = document.getElementById('selectShippingButton');
const shippingOptions = document.querySelectorAll('input[name="shipping"]');

// Function to show the popup
function showShippingPopup() {
  shippingPopup.style.display = 'flex';
}

// Function to hide the popup
function hideShippingPopup() {
  shippingPopup.style.display = 'none';
}

// Event listener to open the popup
openShippingPopup.addEventListener('click', showShippingPopup);

// Event listener to close the popup when the close button is clicked
if (closeShippingPopupButton) {
    closeShippingPopupButton.addEventListener('click', hideShippingPopup);
  }

// Event listener to close the popup when clicking outside the content
window.addEventListener('click', (event) => {
  if (event.target === shippingPopup) {
    hideShippingPopup();
  }
});

// Event listener for the "Select" button (handle shipping selection)
selectShippingButton.addEventListener('click', () => {
  let selectedShipping;
  let selectedShippingLabel;
  shippingOptions.forEach(option => {
    if (option.checked) {
      selectedShipping = option.value;
      selectedShippingLabel = document.querySelector(`label[for="${option.id}"]`).textContent;
    }
  });

  if (selectedShipping) {
    // Update the UI with the selected shipping option
    // For example, update a span with the selected option:
    const selectedShippingDisplay = document.getElementById('selectedShippingOption');
    if (selectedShippingDisplay) {
      selectedShippingDisplay.textContent = selectedShippingLabel;
    }

    alert(`You selected: ${selectedShippingLabel}`);
    // Here you would typically update the UI or proceed with the checkout process.
    hideShippingPopup();
  } else {
    alert('Please select a shipping option.');
  }
});