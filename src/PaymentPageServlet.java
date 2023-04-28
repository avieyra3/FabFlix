import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.io.PrintWriter;

/**
 * This PaymentPageServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/payment.
 */
@WebServlet(name = "PaymentPageServlet", urlPatterns = "/api/payment")
public class PaymentPageServlet extends HttpServlet{

    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * handles GET requests to get Total Price in the shopping cart
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("PaymentPageServlet doGet Executing!");
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        Integer totalPrice = (Integer) session.getAttribute("totalCartPrice");
        PrintWriter out = response.getWriter();

        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("totalCartPrice", totalPrice);
        jsonArray.add(jsonObject);

        request.getServletContext().log("getting " + jsonArray.size() + " results");
        out.write(jsonArray.toString());
    }

    /**
     * handles POST requests to authorize payment
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("PaymentPageServlet doPost Executing!");
        response.setContentType("application/json");

        String cardNumber = request.getParameter("card-number");
        String firstName = request.getParameter("first-name");
        String lastName = request.getParameter("last-name");
        String expireDate = request.getParameter("expire-date");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("ShoppingCart Connection established!\n");
            Statement statement = connection.createStatement();
            String query = "SELECT *\n" +
                    "FROM creditcards\n" +
                    "WHERE id = '" + cardNumber + "' AND firstName = '" + firstName +
                    "' AND lastName = '" + lastName + "' AND expirationDate = '" + expireDate + "';";
            System.out.println(query);
            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonObject = new JsonObject();
            if (result.next()) {
                jsonObject.addProperty("authorized", "true");
            } else {
                jsonObject.addProperty("authorized", "false");
            }
            jsonArray.add(jsonObject);

            result.close();
            statement.close();

            request.getServletContext().log("getting " + jsonArray.size() + " results");

            out.write(jsonArray.toString());
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ERROR:", e.getMessage());
            out.write(jsonObject.toString());

            response.setStatus(500);
        } finally {
            out.close();
        }

        System.out.println("PaymentPageServlet doPost Done!");
    }
}
