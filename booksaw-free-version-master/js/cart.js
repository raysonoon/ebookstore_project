// Select all quantity buttons
document.querySelectorAll(".cart-actions").forEach((cartItem) => {
    const minusBtn = cartItem.querySelector(".quantity-btn:first-child"); // Select '-'
    const plusBtn = cartItem.querySelector(".quantity-btn:last-child"); // Select '+'
    const quantitySpan = cartItem.querySelector("span"); // Select quantity number
    const removeBtn = cartItem.parentElement.querySelector(".remove-btn"); // Select Remove button
    const itemPrice = cartItem.parentElement.querySelector(".item-price"); // Select item price

    let quantity = parseInt(quantitySpan.innerText);
    const pricePerItem = parseFloat(itemPrice.innerText.replace("$", "")); // Extract price

    // Increase quantity
    plusBtn.addEventListener("click", function () {
        quantity++;
        quantitySpan.innerText = quantity;
        updatePrice();
    });

    // Decrease quantity (but not below 1)
    minusBtn.addEventListener("click", function () {
        if (quantity > 1) {
            quantity--;
            quantitySpan.innerText = quantity;
            updatePrice();
        }
    });

    // Remove item from cart
    removeBtn.addEventListener("click", function () {
        cartItem.parentElement.remove();
    });

    // Update total price based on quantity
    function updatePrice() {
        itemPrice.innerText = `$${(pricePerItem * quantity).toFixed(2)}`;
    }
});