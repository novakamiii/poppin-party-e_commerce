export function handleUrlParams() {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('order');
    const status = urlParams.get('status');

    if (orderId && status) {
        // Find and activate the correct tab
        const tab = document.querySelector(`.status-tab[data-tab="${status}"]`);
        if (tab) {
            document.querySelectorAll('.status-tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            loadOrdersByStatus(status);

            // Optional: Scroll to or highlight the specific order
            setTimeout(() => {
                const orderElement = document.querySelector(`[data-order-id="${orderId}"]`);
                if (orderElement) {
                    orderElement.scrollIntoView({ behavior: 'smooth' });
                    orderElement.classList.add('highlighted');
                    setTimeout(() => orderElement.classList.remove('highlighted'), 2000);
                }
            }, 500);
        }
    }
}

export const shippingFees = {
    standard: 45,
    express: 75,
    overnight: 150
};

export function loadOrdersByStatus(status, containerId = "orderStatusContent") {
    fetch(`/api/orders?status=${status}`)
        .then(res => res.json())
        .then(data => {
            const container = document.getElementById(containerId);
            if (!container) {
                console.warn(`Container '${containerId}' not found.`);
                return;
            }

            container.innerHTML = "";

            if (!Array.isArray(data) || data.length === 0) {
                container.innerHTML = `
          <div class="no-orders-found">
            <img src="/img/no-results.png" alt="No orders" class="no-orders-image">
            <p class="no-orders-message">No orders found for this status</p>
          </div>
        `;
                return;
            }

            const grouped = {};
            data.forEach(order => {
                if (!grouped[order.transactionId]) {
                    grouped[order.transactionId] = [];
                }
                grouped[order.transactionId].push(order);
            });

            Object.entries(grouped)
                .sort((a, b) => {
                    const dateA = new Date(a[1][0].order_date);
                    const dateB = new Date(b[1][0].order_date);
                    return dateB - dateA;
                })
                .forEach(([transactionId, orders], index) => {

                    let subtotal = 0;

                    const orderItemsHtml = orders.map(order => {
                        subtotal += parseFloat(order.amount);

                        const etaDays = order.status === "CANCELLED" ? "Cancelled" : (order.daysLeft ?? "N/A");

                        let actionButton = '';
                        if (status === "PENDING") {
                            actionButton = `<button class="order-action-btn cancel-order" data-id="${order.id}">Cancel</button>`;
                        } else if (status === "CANCELLED") {
                            const cancelledDate = new Date(order.order_date);
                            const now = new Date();
                            const hoursDiff = Math.abs(now - cancelledDate) / (1000 * 60 * 60);

                            if (hoursDiff > 24) {
                                actionButton = `<button class="order-action-btn restore-order" data-id="${order.id}" disabled style="opacity:0.6; cursor: not-allowed;">Undo (Expired)</button>`;
                            } else {
                                actionButton = `<button class="order-action-btn restore-order" data-id="${order.id}">Undo</button>`;
                            }
                        } else if (status === "TO_RECEIVE") {
                            actionButton = `<button class="order-action-btn mark-received" data-id="${order.orderId}">Mark as Received</button>`;
                        }

                        const etaHtml = order.status?.toUpperCase() !== "COMPLETED"
                            ? `<p class="item-eta">ETA: ${etaDays} ${etaDays === "Cancelled" ? "" : "day(s) left"}</p>`
                            : "";

                        return `
        <div class="order-item" data-order-id="${order.id}">
            <div class="item-image">
                <img src="${order.imageLoc}" alt="${order.itemName}" />
            </div>
            <div class="item-details">
                <h3 class="item-name">${order.itemName}</h3>
                <p class="tracking-number">Order ID: #${order.id}</p>
                <p class="item-qty">QTY: ${order.quantity}</p>
                ${etaHtml}
            </div>
            <div class="item-total">
                <span class="total-label">Item Price:</span>
                <span class="total-price">₱${parseFloat(order.amount).toLocaleString(undefined, { minimumFractionDigits: 2 })}</span>
                ${actionButton}
            </div>
        </div>
    `;
                    }).join("");


                    const shippingOption = orders[0].shippingOption?.toLowerCase() || "standard";
                    const shippingFee = shippingFees[shippingOption] ?? 45;
                    const tax = subtotal * 0.12;
                    const total = subtotal + tax + shippingFee;

                    const transactionHtml = `
          <div class="order-status-container transaction-group">
            <div class="transaction-header" style="cursor: pointer;" data-toggle="transaction-${index}">
              <h2 class="order-status-title">Transaction ID: ${transactionId} 🔻</h2>
            </div>
            <div class="transaction-body" id="transaction-${index}">
              ${orderItemsHtml}
              <div class="transaction-summary" style="text-align:right; margin-top: 10px;">
                <p class="subtotal">Subtotal: ₱${subtotal.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
                <p class="tax">+ 12% VAT: ₱${tax.toLocaleString(undefined, { minimumFractionDigits: 2 })}</p>
                <p class="shipping">+ Shipping (${shippingOption}): ₱${shippingFee.toLocaleString()}</p>
                <hr>
                <p class="total"><strong>Total: ₱${total.toLocaleString(undefined, { minimumFractionDigits: 2 })}</strong></p>
              </div>
            </div>
          </div>
        `;

                    container.insertAdjacentHTML("beforeend", transactionHtml);
                });

            // Toggle collapsible body
            document.querySelectorAll('.transaction-header').forEach(header => {
                header.addEventListener('click', () => {
                    const body = document.getElementById(header.dataset.toggle);
                    if (body) {
                        body.style.display = body.style.display === 'none' ? 'block' : 'none';
                    }
                });
            });
        })
        .catch(err => {
            const container = document.getElementById(containerId);
            if (container) container.innerHTML = "<p>Error loading orders.</p>";
            console.error("Order fetch error:", err);
        });
}



export function initStatusTabs(tabSelector, containerId, defaultStatus = "PENDING") {
    const tabs = document.querySelectorAll(tabSelector);

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");

            const status = tab.dataset.tab;
            loadOrdersByStatus(status, containerId);
        });
    });

    // Initial load
    loadOrdersByStatus(defaultStatus, containerId);
}


document.addEventListener("DOMContentLoaded", () => {
    document.body.addEventListener("click", e => {
        if (e.target.classList.contains("cancel-order")) {
            e.preventDefault();
            const orderId = e.target.dataset.id;
            if (confirm("Are you sure you want to cancel this item?")) {
                fetch(`/api/orders/cancel/${orderId}`, { method: "POST" })
                    .then(async res => {
                        if (!res.ok) {
                            const errorMessage = await res.text();
                            throw new Error(errorMessage || "Cancel failed");
                        }
                        alert("Cancelled");
                        refreshActiveOrders();
                    })
                    .catch(err => {
                        alert("Error cancelling: " + err.message);
                        console.error("Cancel error:", err);
                    });
            }
        }

        if (e.target.classList.contains("restore-order")) {
            e.preventDefault();
            const orderId = e.target.dataset.id;
            fetch(`/api/orders/restore/${orderId}`, { method: "POST" })
                .then(async res => {
                    if (!res.ok) {
                        const errorMessage = await res.text();
                        throw new Error(errorMessage || "Restore failed");
                    }
                    alert("Restored");
                    refreshActiveOrders();
                })
                .catch(err => {
                    alert("Error restoring: " + err.message);
                    console.error("Restore error:", err);
                });
        }

        // Add this new handler for "Mark as Received"
        if (e.target.classList.contains("mark-received")) {
            e.preventDefault();
            const orderId = e.target.dataset.id;
            if (confirm("Have you received this order?")) {
                fetch(`/orders/${orderId}/mark-received`, { method: "POST" })
                    .then(async res => {
                        if (!res.ok) {
                            const errorMessage = await res.text();
                            throw new Error(errorMessage || "Update failed");
                        }
                        alert("Order marked as received!");
                        refreshActiveOrders();

                        // Optional: Switch to COMPLETED tab
                        const completedTab = document.querySelector('.status-tab[data-tab="COMPLETED"]');
                        if (completedTab) {
                            completedTab.click();
                        }
                    })
                    .catch(err => {
                        alert("Error updating order: " + err.message);
                        console.error("Update error:", err);
                    });
            }
        }
    });

    function refreshActiveOrders() {
        const activeTab = document.querySelector(".status-tab.active");
        const status = activeTab?.dataset.tab || "PENDING";
        const containerId = activeTab?.closest(".dashboard-section")?.querySelector(".order-item-list")?.id || "orderStatusContent";
        loadOrdersByStatus(status, containerId);
    }

});

export function initializeOrderTabs() {
    const tabs = document.querySelectorAll(".status-tab");

    tabs.forEach(tab => {
        tab.addEventListener("click", () => {
            tabs.forEach(t => t.classList.remove("active"));
            tab.classList.add("active");
            loadOrdersByStatus(tab.dataset.tab);
        });
    });

    // Initial load
    loadOrdersByStatus("PENDING");
}
// Add this to your DOMContentLoaded event:
document.addEventListener("DOMContentLoaded", () => {
    initStatusTabs(".status-tab", "orderStatusContent", "PENDING");
    handleUrlParams();  // Add this line
});

