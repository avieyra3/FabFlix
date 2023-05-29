import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

@WebServlet("/movie-suggestion")
public class MovieSuggestionServlet extends HttpServlet {
    private DataSource dataSource;
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    /*
     *
     * Match the query against titles and return a JSON response.
     *
     * For example, if the query is "super":
     * The JSON response look like this:
     * [
     * 	{ "value": "Superman", "data": { "heroID": tt12345678 } },
     * 	{ "value": "Supergirl", "data": { "heroID": tt23456789 } }
     * ]
     *
     * The format is like this because it can be directly used by the
     *   JSON auto complete library this example is using. So that you don't have to convert the format.
     *
     * The response contains a list of suggestions.
     * In each suggestion object, the "value" is the item string shown in the dropdown list,
     *   the "data" object can contain any additional information.
     *
     *
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            // setup the response json arrray
            JsonArray jsonArray = new JsonArray();

            // get the query string from parameter
            String query = request.getParameter("query");
            System.out.println("url query: " + query);
            // return the empty json array if query is null or empty
            if (query == null || query.trim().isEmpty()) {
                response.getWriter().write(jsonArray.toString());
                return;
            }

            // tokenize string based on whitespace
            String[] tokenQueryArr = query.split("\\s+");

            // declare placeholder values
            String likeStr = '%' + query + '%';
            int threshold = (int)Math.round(query.length() * 0.30);
            String ftStr = "";

            // print out tokenQueryArr to check valid output and concatenate ftStr
            String tokens = "Tokens: ";
            for (int i = 0; i < tokenQueryArr.length; i++) {
                tokens += tokenQueryArr[i] + " ";
                ftStr += "+" + tokenQueryArr[i] + "* ";
            }
            System.out.println(tokens + "\nftStr: " + ftStr + "\nlikeStr: " + likeStr + "\nThreshold: " + threshold);
            // set up SQL full search string to concatenate with the query parameters
            // NOTE make sure you have performed the following SQL modification: ALTER TABLE movies ADD FULLTEXT(title);
            // before executing this string!!
            String sqlStr = "select * FROM movies WHERE title LIKE ? OR edth(title, ?, ?) UNION " +
                    "SELECT * FROM movies WHERE MATCH(title) AGAINST ( ? IN BOOLEAN MODE) LIMIT 10";
            System.out.println("Full Search query string: " + sqlStr);

            PrintWriter out = response.getWriter();

            try (Connection conn = dataSource.getConnection()) {
                // create prepared statement for the sql string
                PreparedStatement fullSearchStatement = conn.prepareStatement(sqlStr);
                // update the statement with the appropriate values
                fullSearchStatement.setString(1, likeStr);
                fullSearchStatement.setString(2, query);
                fullSearchStatement.setInt(3, threshold);
                fullSearchStatement.setString(4, ftStr);
                // execute statement and save into results
                ResultSet results = fullSearchStatement.executeQuery();

                while(results.next()) {
                    String movieId = results.getString("id");
                    String movieName = results.getString("title");
                    String movieYear = " (" + results.getString("year") + ")";
                    movieName += movieYear;
                    System.out.println("movieId: " + movieId + " movieName: " + movieName);
                    jsonArray.add(generateJsonObject(movieId, movieName));
                }
                response.getWriter().write(jsonArray.toString());
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

    /*
     * Generate the JSON Object from movie to be like this format:
     * {
     *   "value": "Iron Man",
     *   "data": { "movieId": tt12345678 }
     * }
     *
     */
    private static JsonObject generateJsonObject(String movieId, String movieName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("value", movieName);

        JsonObject additionalDataJsonObject = new JsonObject();
        additionalDataJsonObject.addProperty("movieId", movieId);

        jsonObject.add("data", additionalDataJsonObject);
        return jsonObject;
    }
}