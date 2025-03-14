import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/user")
public class UserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get email passed from the login
        String email = request.getParameter("email");

        if (email != null) {
            try (
                // Connect to the database
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx"); // Replace with your DB username and password

                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?")
            ) {
                pstmt.setString(1, email);
                ResultSet rset = pstmt.executeQuery();

                if (rset.next()) {
                    // Fetch user data from the ResultSet
                    String userEmail = rset.getString("email");
                    String userName = rset.getString("username");
                    String phone_number = rset.getString("phone_number");
                    Double wallet_balance = rset.getDouble("wallet_balance");
                    int userId = rset.getInt("user_id");

                    // Store the user data in the session
                    HttpSession session = request.getSession();
                    session.setAttribute("userEmail", userEmail);
                    session.setAttribute("userName", userName);
                    session.setAttribute("userId", userId);
                    session.setAttribute("phone_number", phone_number);
                    session.setAttribute("wallet_balance", wallet_balance);

                    // Return the user data as JSON response
                    out.print("{"
                            + "\"userEmail\": \"" + userEmail + "\", "
                            + "\"userName\": \"" + userName + "\", "
                            + "\"userId\": " + userId + ", "
                            + "\"phone_number\": \"" + phone_number + "\", "
                            + "\"wallet_balance\": " + wallet_balance
                            + "}");

                    // Optionally, redirect to another page after processing
                    response.sendRedirect("index.html");
                } else {
                    // Handle case if user not found in the database
                    out.print("{\"error\": \"User not found\"}");
                }
            } catch (SQLException ex) {
                out.print("{\"error\": \"" + ex.getMessage() + "\"}");
                ex.printStackTrace();
            }
        } else {
            out.print("{\"error\": \"No email provided\"}");
        }
    }

    // doPost method to handle user data updates
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get updated user details from request parameters
        String email = request.getParameter("email");
        String userName = request.getParameter("userName");
        String phoneNumber = request.getParameter("phone_number");
        Double walletBalance = Double.parseDouble(request.getParameter("wallet_balance"));

        if (email != null) {
            try (
                // Connect to the database
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx"); 

                PreparedStatement pstmt = conn.prepareStatement(
                        "UPDATE users SET username = ?, phone_number = ?, wallet_balance = ? WHERE email = ?")
            ) {
                pstmt.setString(1, userName);
                pstmt.setString(2, phoneNumber);
                pstmt.setDouble(3, walletBalance);
                pstmt.setString(4, email);

                int updatedRows = pstmt.executeUpdate();
                if (updatedRows > 0) {
                    // Update the session with the new user data
                    HttpSession session = request.getSession();
                    session.setAttribute("userEmail", email);
                    session.setAttribute("userName", userName);
                    session.setAttribute("phone_number", phoneNumber);
                    session.setAttribute("wallet_balance", walletBalance);

                    // Return success response
                    out.print("{\"success\": \"User information updated successfully\"}");
                } else {
                    out.print("{\"error\": \"No rows updated. User not found\"}");
                }
            } catch (SQLException ex) {
                // Log the exception and send a detailed error message
                ex.printStackTrace();
                out.print("{\"error\": \"Database error: " + ex.getMessage() + "\"}");
            }
        } else {
            out.print("{\"error\": \"Email is required\"}");
        }
    }


}
