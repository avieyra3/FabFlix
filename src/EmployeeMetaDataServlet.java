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
import java.util.ArrayList;
@WebServlet(name = "EmployeeMetaDataServlet", urlPatterns = "/api/index-employee")
public class EmployeeMetaDataServlet extends HttpServlet {

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

        System.out.println("MetaData doGet EXECUTING");
        response.setContentType("application/json");

        String requestType = request.getParameter("request-type");
        System.out.println(requestType);

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("EmployeeMetaDataServlet Connection established!\n");
            String tableNameQuery = "SHOW tables";
            PreparedStatement statement = connection.prepareStatement(tableNameQuery);
            ResultSet result = statement.executeQuery();
            System.out.println("tableNameQuery executed!");
            JsonArray jsonArray = new JsonArray();

            while (result.next()) {
                // get table name from result set and to new query via placeholder
                String tableName = result.getString(1);
                System.out.println("tableName: " + tableName);
                String tableDetailsQuery = "DESCRIBE " + tableName + ";";
                System.out.println("tableDetailsQuery: " + tableDetailsQuery);

                // get new connection and execute query for details
                PreparedStatement detailsStatement = connection.prepareStatement(tableDetailsQuery);
                ResultSet resultDetails = detailsStatement.executeQuery();
                System.out.println("detailsStatement executed!");

                // create json object to store the info for js file
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("table", tableName);

                // need to create separate jsonArrays for field and type to store multiple fields and types
                JsonArray fieldList = new JsonArray();
                JsonArray typeList = new JsonArray();

                // iterate through the each field and type
                while (resultDetails.next()) {
                    //get the column name and type
                    String field = resultDetails.getString("Field");
                    String type = resultDetails.getString("Type");

                    // insert into array lists
                    fieldList.add(field);
                    typeList.add(type);

                    System.out.println("Table: " + tableName + " fields: " + field + " type: " + type);
                }
                jsonObject.add("field", fieldList);
                jsonObject.add("type", typeList);
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
