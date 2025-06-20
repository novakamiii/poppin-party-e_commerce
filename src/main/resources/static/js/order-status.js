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
                container.innerHTML = "<p>No orders found for this status.</p>";
                return;
            }

            data.forEach(order => {
                const etaDays = order.status === "CANCELLED" ? "Cancelled" : (order.daysLeft ?? "N/A");

                container.insertAdjacentHTML("beforeend", `
                    <div class="order-item">
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
                            ${status === "PENDING" ?
                        `<span><a href="#" class="cancel-order" data-id="${order.id}">Cancel</a></span>` :
                        order.status === "CANCELLED" ?
                            `<span><a href="#" class="restore-order" data-id="${order.id}">Undo</a></span>` :
                            ""
                    }

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
