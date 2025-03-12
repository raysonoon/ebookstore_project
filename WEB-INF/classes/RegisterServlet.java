import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/register")  // Map this servlet to "/register"
public class RegisterServlet extends HttpServlet {

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {

      // Set response type
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      // Get form data
      String username = request.getParameter("username");
      String email = request.getParameter("email");
      String password = request.getParameter("password");
      String phoneNumber = request.getParameter("phone_number");
      String role = "user"; // Default role
      double walletBalance = 0.0; // Default wallet balance

      try (
         // Connect to the database
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "xxxx"); // Replace with your DB username and password

         // Use PreparedStatement to insert new user
         PreparedStatement pstmt = conn.prepareStatement(
               "INSERT INTO users (username, password, email, phone_number, role, wallet_balance) VALUES (?, ?, ?, ?, ?, ?)");
      ) {
         // Set the parameters for the PreparedStatement
         pstmt.setString(1, username);
         pstmt.setString(2, password);
         pstmt.setString(3, email);
         pstmt.setString(4, phoneNumber);
         pstmt.setString(5, role);
         pstmt.setDouble(6, walletBalance);

         // Execute the insertion
         int rowsAffected = pstmt.executeUpdate();

         // Check if insertion was successful
         if (rowsAffected > 0) {
            // Account created successfully, redirect to login page
            response.sendRedirect("login.html");
         } else {
            // Error in account creation, show error message
            out.println("<script type='text/javascript'>");
            out.println("alert('Error creating account. Please try again.');");
            out.println("window.location.href='register.html';");
            out.println("</script>");
         }
      } catch (SQLException ex) {
         out.println("<p>Error: " + ex.getMessage() + "</p>");
         ex.printStackTrace();
      }
   }
}
