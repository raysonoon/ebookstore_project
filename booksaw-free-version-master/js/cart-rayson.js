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
        const bookId = removeBtn.getAttribute("data-id"); // Retrieve the book's ID
        removeFromCart(bookId);
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

    async function fetchCartData() {
        try {
            const response = await fetch('/ebookstore_project/cart-rayson', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                const cartItems = await response.json();

                if (cartItems.length === 0) {
                    document.getElementById("cart-items").innerHTML = "<p>Your cart is empty.</p>";
                } else {
                    let totalPrice = 0;
                    let cartItemsHTML = '';

                    cartItems.forEach(item => {
                        const itemPrice = item.price * item.quantity;
                        totalPrice += itemPrice;

                        cartItemsHTML += `
                            <div class="cart-item">
			                    <div class="container-left">
				                    <img src="images/product-item1.jpg" alt="Book 1" class="cart-image">
				                    <div class="cart-details">
					                    <h4>${item.title}</h4>
					                    <h4 class="item-price">$${item.price.toFixed(2)}</h4>
					                    <div class="cart-actions">
                                            <div class="btn btn-rounded btn-outline-accent quantity-btn">-</div>
                                            <span>${item.quantity}</span>
                                            <div class="btn btn-rounded btn-outline-accent quantity-btn">+</div>
                                        </div>
                                    </div>
                                </div>
			                    <div class="container-right">
				                        <h4 class="sub-total">Total: $${itemPrice.toFixed(2)}</h4>
                                        <div class="btn btn-rounded btn-outline-accent remove-btn" data-id="${item.id}">Remove</div>
                                </div>
			                </div>
                      `;
                    });

                    document.getElementById("cart-items").innerHTML = cartItemsHTML;
                    document.getElementById("cart-total").innerText = `Cart Total: $${totalPrice.toFixed(2)}`;
                }
            } else {
                document.getElementById("cart-items").innerHTML = "<p>Failed to load cart data. Please try again.</p>";
            }
        } catch (error) {
            console.error("Error fetching cart data:", error);
            document.getElementById("cart-items").innerHTML = "<p>There was an error loading your cart. Please try again.</p>";
        }
    }

    async function removeFromCart(bookId) {
        if (!bookId) {
            console.error("Error: Missing book ID for removal.");
            return;
        }

        try {
            const response = await fetch(`/ebookstore_project/cart-rayson/${bookId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (response.ok) {
                await fetchCartData(); // Update the cart display
            } else {
                const errorData = await response.json();
                alert(errorData.error || "Failed to remove item from cart. Please try again.");
            }
        } catch (error) {
            console.error("Error removing item from cart:", error);
            alert("There was an error removing the item. Please try again.");
        }
    }

    // Fetch and display cart data when the page loads
    window.onload = fetchCartData;
});
