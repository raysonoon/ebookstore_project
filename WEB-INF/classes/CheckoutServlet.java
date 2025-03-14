import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            try (
                Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                    "myuser", "xxxx"); 
            ) {
                conn.setAutoCommit(false); // Start transaction

                // Fetch user's cart ID and total price
                PreparedStatement fetchCartStmt = conn.prepareStatement(
                    "SELECT cart_book.cart_id, SUM(cart_book.quantity * books.price) AS total_price FROM cart_book " +
                    "JOIN books ON cart_book.book_id = books.book_id " +
                    "WHERE cart_book.cart_id = (SELECT cart.cart_id FROM cart WHERE cart.user_id = ?) " +
                    "GROUP BY cart_book.cart_id");
                fetchCartStmt.setInt(1, userId);
                ResultSet cartResult = fetchCartStmt.executeQuery();

                if (cartResult.next()) {
                    int cartId = cartResult.getInt("cart_id");
                    double totalPrice = cartResult.getDouble("total_price");

                    // Insert into order_detail
                    PreparedStatement createOrderStmt = conn.prepareStatement(
                        "INSERT INTO order_detail (user_id, total_price, status) VALUES (?, ?, 'purchased')",
                        Statement.RETURN_GENERATED_KEYS);
                    createOrderStmt.setInt(1, userId);
                    createOrderStmt.setDouble(2, totalPrice);
                    createOrderStmt.executeUpdate();

                    // Get generated order_id
                    ResultSet generatedKeys = createOrderStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);

                        // Insert into order_book
                        PreparedStatement insertOrderBookStmt = conn.prepareStatement(
                        "INSERT INTO order_book (order_id, book_id, quantity) " +
                        "SELECT ?, cart_book.book_id, cart_book.quantity FROM cart_book WHERE cart_book.cart_id = ?");
                        
                        insertOrderBookStmt.setInt(1, orderId);
                        insertOrderBookStmt.setInt(2, cartId);
                        insertOrderBookStmt.executeUpdate();

                        // Update book quantities in books table
                        PreparedStatement updateBookStmt = conn.prepareStatement(
                            "UPDATE books SET quantity = quantity - ? WHERE book_id = ?");
                        
                        // For each book in the cart, decrease the stock based on the quantity
                        PreparedStatement cartBooksStmt = conn.prepareStatement(
                            "SELECT book_id, quantity FROM cart_book WHERE cart_id = ?");
                        cartBooksStmt.setInt(1, cartId);
                        ResultSet cartBooksResult = cartBooksStmt.executeQuery();

                        while (cartBooksResult.next()) {
                            int bookId = cartBooksResult.getInt("book_id");
                            int quantity = cartBooksResult.getInt("quantity");
                            
                            updateBookStmt.setInt(1, quantity);
                            updateBookStmt.setInt(2, bookId);
                            updateBookStmt.executeUpdate();
                        }

                        // Clear the cart
                        PreparedStatement clearCartStmt = conn.prepareStatement(
                            "DELETE FROM cart_book WHERE cart_id = ?");
                        clearCartStmt.setInt(1, cartId);
                        clearCartStmt.executeUpdate();

                        // Reset cart total_quantity
                        PreparedStatement resetCartStmt = conn.prepareStatement(
                            "UPDATE cart SET total_quantity = 0 WHERE cart_id = ?");
                        resetCartStmt.setInt(1, cartId);
                        resetCartStmt.executeUpdate();

                        conn.commit(); // Commit transaction

                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write("{\"message\": \"Checkout successful\"}");
                    } else {
                        conn.rollback();
                        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                        response.getWriter().write("{\"error\": \"Order creation failed.\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("{\"error\": \"Cart not found or empty.\"}");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"SQL Error: " + ex.getMessage() + "\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"User not logged in.\"}");
        }
    }
}
