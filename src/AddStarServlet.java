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

@WebServlet(name = "AddStarServlet", urlPatterns = "/api/add-star")
public class AddStarServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DataSource masterSource;

    public void init(ServletConfig config) {
        try {
            masterSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/masterdb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("AddStarServlet doPost EXECUTING");
        response.setContentType("application/json");
        //get the star name and birth year from the request
        String starName = request.getParameter("starName");
        String birthYearStr = request.getParameter("birthYear");
        Integer birthYear = null;

        // convert birthYear to Integer if not null
        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid birthYear format. It should be a number.");
                return;
            }
        }
        System.out.println("\nstarName: " + starName + " birth year: " + birthYear + "\n");
        PrintWriter out = response.getWriter();

        try (Connection connection = masterSource.getConnection()) {
            System.out.println("AddStarServlet Connection established!\n");

            // create call to procedure
            String procedure = "CALL add_star(?, ?, @starId)";
            PreparedStatement addStar = connection.prepareStatement(procedure);
            // set values for placeholder
            addStar.setString(1, starName);
            System.out.println("star name added!");
            if (birthYear == null) {
                addStar.setNull(2, java.sql.Types.INTEGER); // this will set null of type Integer
                System.out.println("birth year added!");
            }
            else {
                addStar.setInt(2, birthYear);
                System.out.println("birth year added!");
            }
            // since returning and updating, we use execute()
            boolean hasResults = addStar.execute();
            String newStarId = null;
            if (hasResults) {
                ResultSet resultSet = addStar.getResultSet();
                if (resultSet.next()) {
                    newStarId = resultSet.getString("starId");
                    System.out.println("New Star ID: " + newStarId);
                }
            }

            System.out.println("addStar executed!");
            //Add the newStarId to the response
            JsonObject responseJsonObject = new JsonObject();
            responseJsonObject.addProperty("id", newStarId);
            response.getWriter().write(responseJsonObject.toString());

            addStar.close();
            response.setStatus(200);

        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("ERROR:", e.getMessage());
            out.write(jsonObject.toString());
            e.printStackTrace();

            response.setStatus(500);
        } finally {
            out.close();
        }
    }
}