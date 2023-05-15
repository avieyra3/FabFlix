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
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * This PurchaseConfirmServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/confirm.
 */
@WebServlet(name = "PurchaseConfirmServlet", urlPatterns = "/api/confirm")
public class PurchaseConfirmServlet extends HttpServlet {
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
     * handles GET requests to register new sale into the database, and return sale data to frontend
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("\n-------PurchaseConfirmServlet doGet Executing!");
        response.setContentType("application/json");

        HttpSession session = request.getSession();
        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        HashMap<String, Integer> previousPrices = (HashMap<String, Integer>) session.getAttribute("previousPrices");
        ArrayList<Integer> previousSales = (ArrayList<Integer>) session.getAttribute("previousSales");
        Integer totalCartPrice = (Integer) session.getAttribute("totalCartPrice");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("PurchaseConfirm Connection established!\n");

            JsonArray jsonArray = new JsonArray();

            for (int saleID : previousSales) {
                //Add sales data from session's cache to the JSON response
                String querySalesData = "SELECT sales.id as saleId, movieId, title, count\n" +
                        "FROM sales, movies\n" +
                        "WHERE sales.movieId = movies.id AND sales.id = ?;\n";
                System.out.println(querySalesData);

                PreparedStatement statementSaleData = connection.prepareStatement(querySalesData);
                statementSaleData.setInt(1, saleID);
                ResultSet resultSalesData = statementSaleData.executeQuery();

                while (resultSalesData.next()) {
                    String movieTitle = resultSalesData.getString("title");
                    String movieID = resultSalesData.getString("movieId");
                    Integer movieCount = resultSalesData.getInt("count");
                    Integer moviePrice = previousPrices.get(movieID);

                    JsonObject jsonObject = new JsonObject();
                    jsonObject.addProperty("sale_id", saleID);
                    jsonObject.addProperty("movie_id", movieID);
                    jsonObject.addProperty("movie_title", movieTitle);
                    jsonObject.addProperty("movie_count", movieCount);
                    jsonObject.addProperty("movie_price", moviePrice);
                    jsonObject.addProperty("total_cart_price", totalCartPrice);
                    jsonArray.add(jsonObject);
                }

                statementSaleData.close();
                resultSalesData.close();
            }
            //Clear cart, aka previousItems
            previousItems.clear();

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

        System.out.println("-------PurchaseConfirmServlet doGet Done!\n");
    }
}
