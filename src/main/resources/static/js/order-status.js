export function loadOrdersByStatus(status) {
    fetch(`/api/orders?status=${status}`)
        .then(res => res.json())
        .then(data => {
            console.log("Fetched response:", data); // 👈 Add this

            const container = document.getElementById("orderStatusContent");
            container.innerHTML = "";

            if (!Array.isArray(data)) {
                container.innerHTML = "<p>Error: Expected a list of orders but got something else.</p>";
                return;
            }

            if (data.length === 0) {
                container.innerHTML = "<p>No orders found for this status.</p>";
                return;
            }

            data.forEach(order => {
                const etaDays = order.daysLeft ?? "N/A";
                container.insertAdjacentHTML("beforeend", `
                    <div class="order-item">
                        <div class="item-image">
                            <img src="${order.imageLoc}" alt="${order.itemName}" />
                        </div>
                        <div class="item-details">
                            <h3 class="item-name">${order.itemName}</h3>
                            <p class="item-qty">QTY: ${order.quantity}</p>
                            <p class="item-eta">ETA: ${etaDays} day(s) left</p>
                        </div>
                        <div class="item-total">
                            <span class="total-label">TOTAL:</span>
                            <span class="total-price">₱${order.amount.toFixed(2)}</span>
                            ${status === "PENDING" ? `<span><a href="#" class="cancel-order" data-id="${order.id}">Cancel</a></span>` : ""}
                        </div>
                    </div>
                `);
            });
        })
        .catch(err => {
            document.getElementById("orderStatusContent").innerHTML = "<p>Error loading orders.</p>";
            console.error("Order fetch error:", err); // 👈 Already present
        });
}

document.addEventListener("DOMContentLoaded", () => {
    const container = document.getElementById("orderStatusContent");
    container.addEventListener("click", e => {
        if (e.target.classList.contains("cancel-order")) {
            e.preventDefault();
            const orderId = e.target.dataset.id;
            if (confirm("Are you sure you want to cancel this order?")) {
                fetch(`/api/orders/cancel/${orderId}`, { method: "POST" })
                    .then(res => {
                        if (!res.ok) throw new Error("Failed to cancel");
                        alert("Order cancelled.");
                        const activeTab = document.querySelector(".status-tab.active");
                        if (activeTab) loadOrdersByStatus(activeTab.dataset.tab);
                    })
                    .catch(err => alert("Error cancelling order."));
            }
        }
    });
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
