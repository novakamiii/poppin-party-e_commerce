

/**
 * Handles URL parameters to activate the correct order status tab and optionally highlight a specific order.
 * Looks for 'order' and 'status' parameters in the URL, activates the corresponding tab,
 * loads orders for that status, and highlights the specified order if present.
 */
//export function handleUrlParams() {}

/**
 * Loads orders by status and renders them into the specified container.
 * Fetches orders from the API, displays them, and adds appropriate action buttons based on status.
 *
 * @param {string} status - The status of orders to load (e.g., "PENDING", "CANCELLED", "TO_RECEIVE").
 * @param {string} [containerId="orderStatusContent"] - The ID of the container to render orders into.
 */
//export function loadOrdersByStatus(status, containerId = "orderStatusContent") {}

/**
 * Initializes status tabs for order filtering.
 * Adds click event listeners to tabs, loads orders for the selected status, and sets the default tab.
 *
 * @param {string} tabSelector - CSS selector for the status tabs.
 * @param {string} containerId - The ID of the container to render orders into.
 * @param {string} [defaultStatus="PENDING"] - The default status to load on initialization.
 */
//export function initStatusTabs(tabSelector, containerId, defaultStatus = "PENDING") {}

/**
 * Initializes order status tabs and sets up click handlers.
 * Loads orders for the selected tab and sets the default tab to "PENDING".
 * (Legacy function, use `initStatusTabs` for more flexibility.)
 */
//export function initializeOrderTabs() {}


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

            if (!Array.isArray(data)) {
                container.innerHTML = "<p>Error: Expected a list of orders.</p>";
                return;
            }

            if (data.length === 0) {
                container.innerHTML =
                    `<div class="no-orders-found">
                        <img src="/img/no-results.png" alt="No orders" class="no-orders-image">
                        <p class="no-orders-message">No orders found for this status</p>
                    </div>
                `;

                return;
            }


            data.forEach(order => {
                const etaDays = order.status === "CANCELLED" ? "Cancelled" : (order.daysLeft ?? "N/A");

                let actionButton = '';
                if (status === "PENDING") {
                    actionButton = `<button class="order-action cancel-order" data-id="${order.id}">Cancel</button>`;
                } else if (status === "CANCELLED") {
                    actionButton = `<button class="order-action restore-order" data-id="${order.id}">Undo</button>`;
                } else if (status === "TO_RECEIVE") {
                    actionButton = `<button class="order-action mark-received" data-id="${order.orderId}">Mark as Received</button>`;
                }

                container.insertAdjacentHTML("beforeend", `
        <div class="order-item" data-order-id="${order.id}">
            <div class="item-image">
                <img src="${order.imageLoc}" alt="${order.itemName}" />
            </div>
            <div class="item-details">
                <h3 class="item-name">${order.itemName}</h3>
                <h3 class="tracking-number">${order.transactionId}</h3>
                <p class="item-qty">QTY: ${order.quantity}</p>
                <p class="item-eta">ETA: ${etaDays} ${etaDays === "Cancelled" ? "" : "day(s) left"}</p>
            </div>
            <div class="item-total">
                <span class="total-label">TOTAL:</span>
                <span class="total-price">₱${order.amount.toFixed(2)}</span>
                <div class="order-actions">
                    ${actionButton}
                </div>
            </div>
        </div>
    `);
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

