import java.io.*;
import java.sql.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {

      // Set response type
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();

      // Get email and password from form
      String email = request.getParameter("email");
      String password = request.getParameter("password");

      try (
         // Connect to the database
         Connection conn = DriverManager.getConnection(
               "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
               "myuser", "xxxx"); // Replace with your DB username and password

         // Use PreparedStatement to prevent SQL Injection
         PreparedStatement pstmt = conn.prepareStatement(
               "SELECT * FROM users WHERE email = ? AND password = ?");
      ) {
         pstmt.setString(1, email);
         pstmt.setString(2, password);
         ResultSet rset = pstmt.executeQuery();

         if (rset.next()) {
            // Redirect to UserServlet and pass the email as a parameter
            response.sendRedirect("user?email=" + email); // Pass email to UserServlet as a parameter
            
         } else {
            // Login failed -> Show error message
            out.println("<script type='text/javascript'>");
            out.println("alert('Invalid email or password. Please try again.');");
            out.println("window.location.href='login.html';"); // Redirect back to login page
            out.println("</script>");
         }
      } catch (SQLException ex) {
         out.println("<p>Error: " + ex.getMessage() + "</p>");
         ex.printStackTrace();
      }
   }
}
