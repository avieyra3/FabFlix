import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

// Declaring a WebServlet called SingleMovieServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("SingleMovie doGet EXECUTING");
        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            System.out.println("SingleMovie Connection established!\n");
            // Get a connection from dataSource

            // Construct a query with parameter represented by "?"
            String query =
                    "SELECT DISTINCT movies.id, movies.title, movies.year, movies.director, rating\n" +
                            "FROM movies JOIN ratings JOIN stars_in_movies JOIN stars JOIN genres_in_movies JOIN genres\n" +
                            "WHERE movies.id = ratings.movieId AND movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id \n" +
                            "AND movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id AND movies.id = ?;";
            System.out.println("query: " + query);
            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet result = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (result.next()) {

                String movie_id = result.getString("id");
                String movie_title = result.getString("title");
                String movie_year = result.getString("year");
                String movie_director = result.getString("director");
                String movie_genres = "";
                String movie_stars = "";
                String star_id = "";
                String movie_rating = result.getString("rating");

                String queryGenres = "SELECT genres.name\n" +
                        "FROM movies JOIN genres_in_movies JOIN genres\n" +
                        "WHERE movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id " +
                        "AND movies.id = ?\n" +
                        "ORDER BY genres.name;";
                System.out.println("queryGenres: " + queryGenres);

                PreparedStatement statementGenres = conn.prepareStatement(queryGenres);
                statementGenres.setString(1, movie_id);
                ResultSet resultGenres = statementGenres.executeQuery();

                while (resultGenres.next()) {
                    movie_genres += resultGenres.getString("name") + "|";
                }
                movie_genres = movie_genres.substring(0, movie_genres.length() - 1);

                String queryStars = "SELECT stars.name, stars.id, count(*) as movie_counts\n" +
                        "FROM movies JOIN stars_in_movies JOIN stars JOIN stars_in_movies as sm2 JOIN movies as m2\n" +
                        "WHERE movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id " +
                        "AND movies.id = ? AND stars.id = sm2.starId AND sm2.moviesId = m2.id\n" +
                        "GROUP BY stars.id\n" +
                        "ORDER BY movie_counts DESC, stars.name ASC";
                System.out.println("queryStars: " + queryStars);

                PreparedStatement statementStars = conn.prepareStatement(queryStars);
                statementStars.setString(1, movie_id);
                ResultSet resultStars = statementStars.executeQuery();

                while (resultStars.next()) {
                    movie_stars += resultStars.getString("name") + "|";
                    star_id += resultStars.getString("id") + "|";
                }
                movie_stars = movie_stars.substring(0, movie_stars.length() - 1);
                star_id = star_id.substring(0, star_id.length() - 1);


                // Create a JsonObject based on the data we retrieve from rs

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);
                jsonObject.addProperty("star_id", star_id);
                jsonObject.addProperty("movie_rating", movie_rating);

                jsonArray.add(jsonObject);
            }
            result.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
