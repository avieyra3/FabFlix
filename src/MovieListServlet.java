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

@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movielist")
public class MovieListServlet extends HttpServlet {

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

        System.out.println("MovieList doGet EXECUTING");
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("MovieList Connection established!\n");
            Statement statement = connection.createStatement();
            String query = 
            "SELECT m.id AS movie_id, m.title AS title, m.year AS year, m.director AS director, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT g.name SEPARATOR ', '), ', ', 3) AS genres, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.name), ',', 3) AS stars, " +
            "SUBSTRING_INDEX(GROUP_CONCAT(DISTINCT s.id ORDER BY s.name), ',', 3) AS star_id," +
            "r.rating AS rating " +
            "FROM movies m " +
            "JOIN genres_in_movies gm ON m.id = gm.movieId " +
            "JOIN genres g ON gm.genreId = g.id " +
            "JOIN stars_in_movies sm ON m.id = sm.moviesId " +
            "JOIN stars s ON sm.starId = s.id " +
            "JOIN ratings r ON m.id = r.movieId " +
            "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
            "ORDER BY r.rating DESC " +
            "LIMIT 20";
            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            while (result.next()) {
                String movie_id = result.getString("movie_id");
                String movie_title = result.getString("title");
                String movie_year = result.getString("year");
                String movie_director = result.getString("director");
                String movie_genres = result.getString("genres");
                String movie_stars = result.getString("stars");
                String star_id = result.getString("star_id");
                String movie_rating = result.getString("rating");

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