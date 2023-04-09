import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

public class MovieListServlet {

    public static void main(String[] arg) throws Exception {
        System.out.println("asdf");

        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection connection = DriverManager.getConnection("jdbc:mysql:///moviedb?autoReconnect=true&useSSL=false",
                "mytestuser", "My6$Password");

        if (connection != null) {
            System.out.println("Connection established!\n");
            Statement select = connection.createStatement();
            String query = "SELECT * \n" +
                    "FROM movies JOIN ratings\n" +
                    "WHERE movies.id = ratings.movieId\n" +
                    "ORDER BY ratings.rating DESC\n" +
                    "LIMIT 20;";
            ResultSet result = select.executeQuery(query);

            System.out.println("Query result:");
            ResultSetMetaData metadata = result.getMetaData();
            int columnCount = metadata.getColumnCount();
            System.out.println("There are " + columnCount + " columns");


            for (int i = 1; i <= columnCount; i++) {
                result.next();
                System.out.println("Type of column " + i + " is " + metadata.getColumnTypeName(i));
                System.out.println(result.getString(i));
            }

            System.out.println(result);


        } else {
            System.out.printf("No connection");
        }
    }



}
