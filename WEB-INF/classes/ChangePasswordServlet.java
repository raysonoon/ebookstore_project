import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/changePassword")
public class ChangePasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get current password, new password, and email from the request
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        // Get the user ID from the session 
        HttpSession session = request.getSession();
        Integer userId = (Integer) session.getAttribute("userId");

        if (currentPassword != null && newPassword != null && userId != null) {
            try (
                // Connect to the database
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx"); // Replace with your DB username and password
                
                // Get the current password from the database
                PreparedStatement pstmt = conn.prepareStatement("SELECT password FROM users WHERE user_id = ?")
            ) {
                pstmt.setInt(1, userId);
                ResultSet rset = pstmt.executeQuery();

                if (rset.next()) {
                    String storedPassword = rset.getString("password");

                    // Check if the current password matches the one stored in the database
                    if (storedPassword.equals(currentPassword)) {
                        // Update the password
                        try (PreparedStatement updatePstmt = conn.prepareStatement(
                                "UPDATE users SET password = ? WHERE user_id = ?")) {
                            updatePstmt.setString(1, newPassword);
                            updatePstmt.setInt(2, userId);
                            int updatedRows = updatePstmt.executeUpdate();

                            if (updatedRows > 0) {
                                out.print("{\"success\": \"Password updated successfully\"}");
                            } else {
                                out.print("{\"error\": \"Failed to update password. Please try again.\"}");
                            }
                        }
                    } else {
                        out.print("{\"error\": \"Current password is incorrect\"}");
                    }
                } else {
                    out.print("{\"error\": \"User not found\"}");
                }
            } catch (SQLException ex) {
                out.print("{\"error\": \"Database error: " + ex.getMessage() + "\"}");
                ex.printStackTrace();
            }
        } else {
            out.print("{\"error\": \"All fields are required\"}");
        }
    }
}