// Select all cart items
document.querySelectorAll(".cart-item").forEach((cartItem) => {
    const minusBtn = cartItem.querySelector(".quantity-btn:first-child"); // Select '-'
    const plusBtn = cartItem.querySelector(".quantity-btn:last-child"); // Select '+'
    const quantitySpan = cartItem.querySelector("span"); // Select quantity number
    const removeBtn = cartItem.querySelector(".remove-btn"); // Select Remove button
    const itemPrice = cartItem.querySelector(".item-price"); // Select item price
    const subtotalElement = cartItem.querySelector(".sub-total"); // Select subtotal in container-right
    
    let quantity = parseInt(quantitySpan.innerText);
    const pricePerItem = parseFloat(itemPrice.innerText.replace("$", "")); // Extract price

    // Initialize subtotal display
    updateSubtotal();

    // Increase quantity
    plusBtn.addEventListener("click", function () {
        quantity++;
        quantitySpan.innerText = quantity;
        updateSubtotal();
    });

    // Decrease quantity (but not below 1)
    minusBtn.addEventListener("click", function () {
        if (quantity > 1) {
            quantity--;
            quantitySpan.innerText = quantity;
            updateSubtotal();
        }
    });

    // Remove item from cart
    removeBtn.addEventListener("click", function () {
        cartItem.remove();
        updateCartTotal();
        checkCartEmpty();
    });

    // Update subtotal based on quantity
    function updateSubtotal() {
        const subtotal = pricePerItem * quantity;
        subtotalElement.innerText = `Total: $${subtotal.toFixed(2)}`;
        updateCartTotal();
    }

    // Update cart-total (sum of all subtotals)
    function updateCartTotal() {
        let total = 0;
        document.querySelectorAll(".sub-total").forEach((subtotal) => {
            total += parseFloat(subtotal.innerText.replace("Total: $", ""));
        });

        const cartTotalElement = document.querySelector(".cart-total");
        if (cartTotalElement) {
            cartTotalElement.innerText = `Cart Total: $${total.toFixed(2)}`;
        }
    }

    // Check if cart is empty
    function checkCartEmpty() {
        if (document.querySelectorAll(".cart-item").length === 0) {
            document.querySelector("#cart-items").innerHTML = "<h2>Your cart is empty.</h2>";
        }
    }
});
