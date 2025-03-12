import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

//ensure that the mapping for /cart/{bookId} is correctly set up to handle DELETE requests. for the /* 
@WebServlet("/cart/*")
public class CartServlet extends HttpServlet {

     //retrieve the cart details from database
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         
         response.setContentType("application/json");  // Return JSON response
         response.setCharacterEncoding("UTF-8");
 
         // Get the user ID from the session 
         HttpSession session = request.getSession();
         Integer userId = (Integer) session.getAttribute("userId");
 
         if (userId != null) {
             try (
                 // Connect to the database
                 Connection conn = DriverManager.getConnection(
                         "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                         "myuser", "xxxx"); // Replace with your DB username and password
             ) {
                 // Fetch cart items for the user
                 PreparedStatement cartItemsStmt = conn.prepareStatement(
                         "SELECT b.book_id, b.book_title, b.author_name, b.price, cb.quantity, b.image_url " +
                         "FROM cart_book cb " +
                         "JOIN books b ON cb.book_id = b.book_id " +
                         "JOIN cart c ON cb.cart_id = c.cart_id " +
                         "WHERE c.user_id = ?");
 
                 cartItemsStmt.setInt(1, userId);
                 ResultSet resultSet = cartItemsStmt.executeQuery();
 
                 // Build a JSON array of cart items
                 StringBuilder jsonResponse = new StringBuilder();
                 jsonResponse.append("[");
 
                 while (resultSet.next()) {
                    int bookId = resultSet.getInt("book_id");
                    String title = resultSet.getString("book_title");
                    String author = resultSet.getString("author_name");
                    double price = resultSet.getDouble("price");
                    int quantity = resultSet.getInt("quantity");
                    String image_url = resultSet.getString("image_url");
                
                    jsonResponse.append("{")
                        .append("\"bookId\":").append(bookId).append(",")
                        .append("\"title\":\"").append(title).append("\",")
                        .append("\"author\":\"").append(author).append("\",")
                        .append("\"price\":").append(price).append(",")
                        .append("\"quantity\":").append(quantity).append(",")
                        .append("\"image_url\":\"").append(image_url).append("\"") 
                        .append("},");
                }
                
 
                 // Remove the last comma and close the array
                 if (jsonResponse.length() > 1) {
                     jsonResponse.deleteCharAt(jsonResponse.length() - 1);
                 }
                 jsonResponse.append("]");
 
                 response.getWriter().write(jsonResponse.toString());
             } catch (SQLException ex) {
                 ex.printStackTrace();
                 response.getWriter().write("{\"error\": \"SQL Error: " + ex.getMessage() + "\"}");
             }
         } else {
             response.getWriter().write("{\"error\": \"User not logged in.\"}");
         }
     }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/plain");  // Plain text instead of JSON
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get the user ID from the session 
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            // Read the book_id from the request parameters
            String bookIdParam = request.getParameter("book_id");
            System.out.println("Received book_id: " + bookIdParam); // Log the book_id
            Integer book_id = null;

            if (bookIdParam != null) {
                try {
                    book_id = Integer.parseInt(bookIdParam);
                } catch (NumberFormatException e) {
                    out.print("Failed to add book to cart. Invalid book_id format.");
                    return;
                }
            }

            try (
                // Connect to the database
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx"); // Replace with your DB username and password
            ) {
                // Check if the cart already exists for the user
                PreparedStatement checkCartStmt = conn.prepareStatement(
                        "SELECT cart_id, total_quantity FROM cart WHERE user_id = ?");
                checkCartStmt.setInt(1, userId);
                ResultSet cartResult = checkCartStmt.executeQuery();

                int cartId = 0;
                boolean cartExists = cartResult.next();
                if (cartExists) {
                    // Cart exists, get the cart ID and total quantity
                    cartId = cartResult.getInt("cart_id");
                    int totalQuantity = cartResult.getInt("total_quantity");

                    // Update the total_quantity by adding 1
                    PreparedStatement updateCartStmt = conn.prepareStatement(
                            "UPDATE cart SET total_quantity = ? WHERE cart_id = ?");
                    updateCartStmt.setInt(1, totalQuantity + 1); // Increase quantity by 1
                    updateCartStmt.setInt(2, cartId);
                    updateCartStmt.executeUpdate();
                } else {
                    // Cart does not exist, create a new cart for the user
                    PreparedStatement createCartStmt = conn.prepareStatement(
                            "INSERT INTO cart (user_id, total_quantity) VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
                    createCartStmt.setInt(1, userId);
                    createCartStmt.setInt(2, 1);  // Initial total_quantity is 1
                    createCartStmt.executeUpdate();
                    
                    // Get the generated cart_id for the newly created cart
                    ResultSet generatedKeys = createCartStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        cartId = generatedKeys.getInt(1);
                    }
                }

                // Now handle adding the book to the cart_book table
                PreparedStatement checkCartBookStmt = conn.prepareStatement(
                        "SELECT quantity FROM cart_book WHERE cart_id = ? AND book_id = ?");
                checkCartBookStmt.setInt(1, cartId);
                checkCartBookStmt.setInt(2, book_id);
                ResultSet cartBookResult = checkCartBookStmt.executeQuery();

                if (cartBookResult.next()) {
                    // Book already exists in the cart, update the quantity
                    int existingQuantity = cartBookResult.getInt("quantity");
                    PreparedStatement updateCartBookStmt = conn.prepareStatement(
                            "UPDATE cart_book SET quantity = ? WHERE cart_id = ? AND book_id = ?");
                    updateCartBookStmt.setInt(1, existingQuantity + 1);
                    updateCartBookStmt.setInt(2, cartId);
                    updateCartBookStmt.setInt(3, book_id);
                    updateCartBookStmt.executeUpdate();
                } else {
                    // Insert the book into the cart_book table
                    PreparedStatement insertCartBookStmt = conn.prepareStatement(
                            "INSERT INTO cart_book (cart_id, book_id, quantity) VALUES (?, ?, ?)");
                    insertCartBookStmt.setInt(1, cartId);
                    insertCartBookStmt.setInt(2, book_id);
                    insertCartBookStmt.setInt(3, 1);  // Default quantity is 1
                    insertCartBookStmt.executeUpdate();
                }

                out.print("Book added to cart successfully.");
            } catch (SQLException ex) {
                ex.printStackTrace();
                out.print("SQL Error: " + ex.getMessage());
            }
        } else {
            out.print("User not logged in.");
        }
    }


    //to delete book from cart_book and update the cart table to -1 from the total_quantity
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the user ID from session
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (userId != null) {
            try (
                // Connect to the database
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx"); // Replace with your DB username and password
            ) {
                // Get the bookId from the URL
                String pathInfo = request.getPathInfo(); // /cart/{bookId}
                if (pathInfo != null && pathInfo.startsWith("/")) {
                    pathInfo = pathInfo.substring(1);  // Remove leading slash
                }
                int bookId = Integer.parseInt(pathInfo);
                
                // Fetch the cart_id for the user
                PreparedStatement fetchCartIdStmt = conn.prepareStatement(
                    "SELECT cart_id FROM cart WHERE user_id = ?");
                fetchCartIdStmt.setInt(1, userId);
                ResultSet cartIdResultSet = fetchCartIdStmt.executeQuery();
                int cartId = -1;
                if (cartIdResultSet.next()) {
                    cartId = cartIdResultSet.getInt("cart_id");
                }
                
                // If cart_id exists, proceed with the DELETE and UPDATE
                if (cartId != -1) {
                    // Fetch the quantity of the book to be removed
                    PreparedStatement fetchQuantityStmt = conn.prepareStatement(
                            "SELECT quantity FROM cart_book WHERE cart_id = ? AND book_id = ?");
                    fetchQuantityStmt.setInt(1, cartId);
                    fetchQuantityStmt.setInt(2, bookId);

                    ResultSet resultSet = fetchQuantityStmt.executeQuery();
                    int removedQuantity = 0;
                    if (resultSet.next()) {
                        removedQuantity = resultSet.getInt("quantity");
                    }

                    // DELETE SQL statement to remove the book from the user's cart
                    PreparedStatement deleteStmt = conn.prepareStatement(
                            "DELETE FROM cart_book WHERE cart_id = ? AND book_id = ?");
                    deleteStmt.setInt(1, cartId);
                    deleteStmt.setInt(2, bookId);

                    int rowsAffected = deleteStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        // Update the total_quantity in the cart table
                        PreparedStatement updateCartStmt = conn.prepareStatement(
                                "UPDATE cart SET total_quantity = total_quantity - ? WHERE cart_id = ?");
                        updateCartStmt.setInt(1, removedQuantity);
                        updateCartStmt.setInt(2, cartId);
                        updateCartStmt.executeUpdate();

                        // Successfully removed the book and updated the total quantity
                        response.setStatus(HttpServletResponse.SC_OK);
                    } else {
                        response.setStatus(HttpServletResponse.SC_NOT_FOUND);  // Book or cart not found
                        response.getWriter().write("{\"error\": \"Item not found in the cart.\"}");
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);  // Cart not found
                    response.getWriter().write("{\"error\": \"Cart not found for user.\"}");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                response.getWriter().write("{\"error\": \"SQL Error: " + ex.getMessage() + "\"}");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // User not logged in
            response.getWriter().write("{\"error\": \"User not logged in.\"}");
        }
    }

}
