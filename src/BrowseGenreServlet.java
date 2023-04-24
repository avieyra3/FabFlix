import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "BrowseGenreServlet", urlPatterns = "/api/browse")
public class BrowseGenreServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("Browse doGet EXECUTING");
        response.setContentType("application/json");

        String requestType = request.getParameter("request-type");
        System.out.println(requestType);

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("BrowseServlet Connection established!\n");
            Statement statement = connection.createStatement();
            String query = "SELECT DISTINCT name FROM genres";

            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            while (result.next()) {
                String genre_name = result.getString("name");
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("genre", genre_name);
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
    }
}