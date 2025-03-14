import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.*;

@WebServlet("/getOrderDetails")
public class OrderServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get userId from session
        HttpSession session = request.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");

            if (userId != null) {
                // Fetch order data from the database
                List<String> orders = getOrderDetails(userId);

                if (orders != null && !orders.isEmpty()) {
                    // Send orders as a JSON object with the 'orders' key
                    out.print("{\"orders\": [" + String.join(",", orders) + "]}");
                } else {
                    out.print("{\"orders\": [], \"message\": \"No orders found.\"}");
                }
            } else {
                out.print("{\"error\": \"User ID not found in session.\"}");
            }
        } else {
            out.print("{\"error\": \"No session found.\"}");
        }
    }

    // Method to get order details from the database
    private List<String> getOrderDetails(int userId) {
        List<String> orders = new ArrayList<>();
    
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC", 
                "myuser", "xxxx")) {
    
            // Query to fetch order details and related books for the user
            String query = "SELECT od.order_id, od.total_price, od.status, ob.book_id, b.book_title, b.price, ob.quantity "
                         + "FROM order_detail od "
                         + "JOIN order_book ob ON od.order_id = ob.order_id "
                         + "JOIN books b ON ob.book_id = b.book_id "
                         + "WHERE od.user_id = ?";
    
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId); // Set userId parameter
    
                try (ResultSet rs = stmt.executeQuery()) {
                    Map<Integer, Map<String, Object>> orderMap = new HashMap<>();
    
                    // Iterate through result set to process each order
                    while (rs.next()) {
                        int orderId = rs.getInt("order_id");
                        Map<String, Object> order = orderMap.computeIfAbsent(orderId, k -> {
                            Map<String, Object> orderDetails = new HashMap<>();
                            try {
                                orderDetails.put("order_id", orderId);
                                orderDetails.put("total_price", rs.getDouble("total_price"));
                                orderDetails.put("status", rs.getString("status"));
                                orderDetails.put("books", new ArrayList<Map<String, Object>>());
                            } catch (SQLException e) {
                                e.printStackTrace(); // Handle exception
                            }
                            return orderDetails;
                        });
    
                        // Prepare the book details
                        Map<String, Object> bookDetails = new HashMap<>();
                        bookDetails.put("book_id", rs.getInt("book_id"));
                        bookDetails.put("book_title", rs.getString("book_title"));
                        bookDetails.put("price", rs.getDouble("price"));
                        bookDetails.put("quantity", rs.getInt("quantity"));
    
                        // Add the book to the order
                        ((List<Map<String, Object>>) order.get("books")).add(bookDetails);
                    }
    
                    // Log the order map to see what data has been retrieved
                    System.out.println("Order Map Retrieved: " + orderMap);
    
                    // Convert the order data to JSON strings
                    for (Map<String, Object> order : orderMap.values()) {
                        String orderJson = String.format(
                                "{\"order_id\": \"%d\", \"total_price\": %.2f, \"status\": \"%s\", \"books\": [%s]}",
                                order.get("order_id"),
                                order.get("total_price"),
                                order.get("status"),
                                getBooksJson(order.get("books"))
                        );
                        orders.add(orderJson);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle exception
        }
    
        return orders;
    }
    

    // Helper method to convert book details to JSON format
    private String getBooksJson(Object booksObj) {
        List<Map<String, Object>> books = (List<Map<String, Object>>) booksObj;
        List<String> bookJsonList = new ArrayList<>();

        for (Map<String, Object> book : books) {
            String bookJson = String.format(
                    "{\"book_id\": \"%d\", \"book_title\": \"%s\",\"price\": %.2f, \"quantity\": \"%d\"}",
                    book.get("book_id"),
                    book.get("book_title"),
                    book.get("price"),
                    book.get("quantity")
            );
            bookJsonList.add(bookJson);
        }

        return String.join(",", bookJsonList);
    }
}
