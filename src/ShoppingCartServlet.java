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
 * This ShoppingCartServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/cart.
 */
@WebServlet(name = "ShoppingCartServlet", urlPatterns = "/api/cart")
public class ShoppingCartServlet extends HttpServlet {

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
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("ShoppingCartServlet doGet Executing!");
        response.setContentType("application/json");
        HttpSession session = request.getSession();

        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
        }
        // Convert previousItems from Java ArrayList to SQL list
        String sqlCartList = "(";
        if (previousItems.size() > 0) {
            for (String key : previousItems.keySet()) {
                sqlCartList += "'" + key + "', ";
            }
            sqlCartList = sqlCartList.substring(0, sqlCartList.length() - 2);
        }
        sqlCartList += ")";
        // Log to localhost log
        request.getServletContext().log("getting " + previousItems.size() + " items");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("ShoppingCart Connection established!\n");
            Statement statement = connection.createStatement();
            String query = "SELECT *\n" +
                    "FROM movies\n" +
                    "WHERE movies.id IN " + sqlCartList + ";";
            System.out.println(query);
            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            while (result.next()) {
                String movie_id = result.getString("id");
                String movie_title = result.getString("title");
                Integer movie_count = previousItems.get(movie_id);

                System.out.println(movie_id + " " + movie_title);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_count", movie_count);

                jsonArray.add(jsonObject);
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

        // write all the data into the jsonObject

    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("ShoppingCartServlet doPost Executing!");
        String item = request.getParameter("item");
        System.out.println(item);
        HttpSession session = request.getSession();

        // get the previous items in a ArrayList
        HashMap<String, Integer> previousItems = (HashMap<String, Integer>) session.getAttribute("previousItems");
        if (previousItems == null) {
            previousItems = new HashMap<String, Integer>();
            previousItems.put(item, 1);
            session.setAttribute("previousItems", previousItems);
        } else {
            // prevent corrupted states through sharing under multi-threads
            // will only be executed by one thread at a time
            synchronized (previousItems) {
                if (previousItems.containsKey(item)) {
                    previousItems.put(item, previousItems.get(item) + 1);
                } else {
                    previousItems.put(item, 1);
                }
            }
        }

//        JsonObject responseJsonObject = new JsonObject();
//
//        JsonArray previousItemsJsonArray = new JsonArray();
//        previousItems.forEach(previousItemsJsonArray::add);
//        responseJsonObject.add("previousItems", previousItemsJsonArray);
//
//        response.getWriter().write(responseJsonObject.toString());
    }
}
