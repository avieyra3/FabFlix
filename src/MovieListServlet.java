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

        System.out.println("DOGET EXECUTING");
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Connection established!\n");
            Statement statement = connection.createStatement();
            String query = "SELECT * \n" +
                    "FROM movies JOIN ratings\n" +
                    "WHERE movies.id = ratings.movieId\n" +
                    "ORDER BY ratings.rating DESC\n" +
                    "LIMIT 20;";
            ResultSet result = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            while (result.next()) {
                String movie_id = result.getString("id");
                String movie_title = result.getString("title");
                String movie_year = result.getString("year");
                String movie_director = result.getString("director");
                String movie_rating = result.getString("rating");
                String movie_genres = "";
                String movie_stars = "";

                Statement statementGenres = connection.createStatement();
                String queryGenres = "SELECT genres.name\n" +
                        "FROM movies JOIN genres_in_movies JOIN genres\n" +
                        "WHERE movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id " +
                        "AND movies.id = '" + movie_id + "'\n" +
                        "LIMIT 3;";
                ResultSet resultGenres = statementGenres.executeQuery(queryGenres);
                while (resultGenres.next()) {
                    movie_genres += resultGenres.getString("name") + ", ";
                }

                Statement statementStars = connection.createStatement();
                String queryStars = "SELECT stars.name\n" +
                        "FROM movies JOIN stars_in_movies JOIN stars\n" +
                        "WHERE movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id " +
                        "AND movies.id = '" + movie_id + "'\n" +
                        "LIMIT 3;";
                ResultSet resultStars = statementGenres.executeQuery(queryStars);
                while (resultStars.next()) {
                    movie_stars += resultStars.getString("name") + ", ";
                }

                System.out.println(movie_id + " " + movie_title + " " + movie_year + " " + movie_director
                        + " " + movie_rating + " " + movie_genres + " " + movie_stars);

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
                jsonObject.addProperty("movie_genres", movie_genres);
                jsonObject.addProperty("movie_stars", movie_stars);

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
