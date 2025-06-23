document.getElementById("addToCartBtn").addEventListener("click", () => {
    const size = document.querySelector('input[name="size"]:checked').value;
    const price = parseFloat(document.querySelector('input[name="size"]:checked').dataset.price);
    const eventType = document.getElementById('type-of-event').value;
    const message = document.getElementById('personalized-message').value;
    const thickness = document.getElementById('tarpaulin-thickness').value;
    const finish = document.getElementById('tarpaulin-finish').value;

    const totalPrice = calculateEstimatedPrice(); // implement this or reuse existing logic

    fetch("/api/cart/custom-tarpaulin", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            size, eventType, message, thickness, finish, price: totalPrice
        })
    })
        .then(res => {
            if (res.ok) {
                alert("Custom tarpaulin added!");
            } else {
                return res.text().then(msg => {
                    console.error("Server Error:", msg);
                    alert("Failed to add to cart.");
                });
            }
        })
        .catch(err => {
            console.error("Fetch failed:", err);
            alert("An error occurred.");
        });

});

function calculateEstimatedPrice() {
    const sizePrice = parseFloat(document.querySelector('input[name="size"]:checked').dataset.price);
    const thickness = document.getElementById('tarpaulin-thickness').value;
    const finish = document.getElementById('tarpaulin-finish').value;

    let thicknessCost = 0;
    let finishCost = 0;

    if (thickness === 'thick') thicknessCost = 50;
    else if (thickness === 'extra-thick') thicknessCost = 100;

    if (finish === 'glossy') finishCost = 30;

    return sizePrice + thicknessCost + finishCost;
}

