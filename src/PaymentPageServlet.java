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
import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.io.PrintWriter;
import java.util.HashMap;

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
        System.out.println("\n-------PaymentPageServlet doGet Executing!");
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
        System.out.println("-------PaymentPageServlet doGet Done!\n");
    }

    /**
     * handles POST requests to authorize payment
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("\n-------PaymentPageServlet doPost Executing!");
        response.setContentType("application/json");
        HttpSession session = request.getSession();
        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");

        String cardNumber = request.getParameter("card-number");
        String firstName = request.getParameter("first-name");
        String lastName = request.getParameter("last-name");
        String expireDate = request.getParameter("expire-date");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("ShoppingCart Connection established!\n");
            ArrayList<Integer> previousSales = (ArrayList<Integer>) session.getAttribute("previousSales");
            if (previousSales == null) {
                previousSales = new ArrayList<Integer>();
            } else {
                //Clear sales cache, aka previousSales
                previousSales.clear();
            }

            // Check if credit card info are correct
            Statement statement = connection.createStatement();
            String query = "SELECT *\n" +
                    "FROM creditcards\n" +
                    "WHERE id = '" + cardNumber + "' AND firstName = '" + firstName +
                    "' AND lastName = '" + lastName + "' AND expirationDate = '" + expireDate + "';\n";
            System.out.println(query);
            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();
            JsonObject jsonAuth = new JsonObject();
            if (result.next()) {
                jsonAuth.addProperty("authorized", "true");
                jsonArray.add(jsonAuth);

                //Get customerID from cardNumber
                Statement statementGetCustomerID = connection.createStatement();
                String queryGetCustomerID = "SELECT id\n" +
                        "FROM customers\n" +
                        "WHERE ccId = '" + cardNumber + "';\n";
                System.out.println(queryGetCustomerID);
                ResultSet resultCustomerID = statementGetCustomerID.executeQuery(queryGetCustomerID);
                System.out.println(resultCustomerID);
                String customerID = "";
                while (resultCustomerID.next()) {
                    customerID = resultCustomerID.getString("id");
                    System.out.println("Card Number: " + cardNumber + " --> customerID: " + customerID + "\n");
                }

                //Insert into sales table with customerID and previousItems
                java.sql.Date todaysDate = new java.sql.Date(System.currentTimeMillis());
                for (String item : previousItems.keySet()) {
                    Statement statementInsertSales = connection.createStatement();
                    String queryInsertSales = "INSERT INTO sales \n" +
                            "VALUES (NULL, '" + customerID + "', '" + item + "', '" +
                            todaysDate + "', " + previousItems.get(item) + ");\n";
                    System.out.println(queryInsertSales);
                    int rowInserted = statementInsertSales.executeUpdate(queryInsertSales);
                    statementInsertSales.close();

                    //Add sales IDs to session's cache
                    Statement statementSaleData = connection.createStatement();
                    String querySalesData = "SELECT LAST_INSERT_ID() AS id;\n";
                    System.out.println(querySalesData);
                    ResultSet resultSalesData = statementSaleData.executeQuery(querySalesData);
                    while (resultSalesData.next()) {
                        int saleID = resultSalesData.getInt("id");
                        synchronized (previousSales) {
                            previousSales.add(saleID);
                        }
                        System.out.println("movieID: " + item + ", saleID: " + saleID + "\n");
                    }
                    synchronized (previousSales) {
                        session.setAttribute("previousSales", previousSales);
                    }
                    statementSaleData.close();
                    resultSalesData.close();
                }
                statementGetCustomerID.close();
                resultCustomerID.close();

            } else {
                jsonAuth.addProperty("authorized", "false");
                jsonArray.add(jsonAuth);
            }

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

        System.out.println("-------PaymentPageServlet doPost Done!\n");
    }
}
