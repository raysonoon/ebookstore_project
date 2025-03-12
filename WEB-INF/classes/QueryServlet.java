import java.io.*;
import java.sql.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/query")
public class QueryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try (
                Connection conn = DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/ebookshop?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC",
                        "myuser", "xxxx");
                Statement stmt = conn.createStatement();
        ) {
            String sqlStr = "SELECT * FROM books";
            ResultSet rset = stmt.executeQuery(sqlStr);

            List<String> books = new ArrayList<>();
            while (rset.next()) {
                String bookJson = String.format(
                        "{\"book_id\":\"%d\",\"title\":\"%s\", \"author\":\"%s\", \"price\":%.2f, \"description\":\"%s\", \"quantity\":\"%d\",\"image\":\"%s\"}",
                        rset.getInt("book_id"),
                        rset.getString("book_title"),
                        rset.getString("author_name"),
                        rset.getDouble("price"),
                        rset.getString("book_description"),
                        rset.getInt("quantity"),
                        rset.getString("image_url") != null ? rset.getString("image_url") : "images/Harry Potter and the Sorcerers Stone.jpg"
                );
                books.add(bookJson);
            }

            out.print("[" + String.join(",", books) + "]");  // Send JSON array
            out.flush();

        } catch (SQLException ex) {
            out.print("{\"error\": \"" + ex.getMessage() + "\"}");
            ex.printStackTrace();
        }
    }
}
