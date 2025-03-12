import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet("/getSessionData")
public class GetSessionDataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Get user data from session
        HttpSession session = request.getSession(false);  // Don't create a new session if it doesn't exist
        if (session != null) {
            String userEmail = (String) session.getAttribute("userEmail");
            String userName = (String) session.getAttribute("userName");
            Integer userId = (Integer) session.getAttribute("userId");
            String phoneNumber = (String) session.getAttribute("phone_number");
            Double walletBalance = (Double) session.getAttribute("wallet_balance");

            // Check if required data is available in session
            if (userEmail != null && userName != null && userId != null) {
                // Create a JSON response with the user's session data
                out.print("{"
                        + "\"userEmail\": \"" + userEmail + "\", "
                        + "\"userName\": \"" + userName + "\", "
                        + "\"userId\": " + userId + ", "
                        + "\"phone_number\": \"" + (phoneNumber != null ? phoneNumber : "") + "\", "
                        + "\"wallet_balance\": " + (walletBalance != null ? walletBalance : 0.0)
                        + "}");
            } else {
                out.print("{\"error\": \"Session data not found\"}");
            }
        } else {
            out.print("{\"error\": \"No session found\"}");
        }
    }
}
