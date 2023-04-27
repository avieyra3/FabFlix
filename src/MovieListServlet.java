import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;


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
        System.out.println("request query: " + request.getQueryString());
        String requestType = request.getParameter("request-type");
        System.out.println("request type: " + requestType);

        // These strings are the default/preliminary value before modification
        // this is the data sql query string that will be modified according to search/browse and sort.
        String query = "SELECT DISTINCT movies.id, movies.title, movies.year, movies.director, rating\n" +
                        "FROM movies JOIN ratings JOIN stars_in_movies JOIN stars JOIN genres_in_movies JOIN genres\n" +
                        "WHERE movies.id = ratings.movieId AND movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id \n" +
                        "AND movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id\n";
        String queryAmmend = ""; // this is sql string that will include info from search/browse
        String sortBy = " ORDER BY title ASC, rating ASC"; // the default sort setting
        String pageSize = " LIMIT 10"; // the default page size
        String offset = ""; // offset will be modded via pagination

        if (requestType == null) {
            ;
        } else if (requestType.equals("search")) {
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");

            System.out.println(title + " " + year + " " + director + " " + star);

            queryAmmend = " AND title LIKE '%" + title + "%' AND director LIKE '%" + director + "%' AND stars.name LIKE '%" + star + "%'";
            if (year != "") {
                queryAmmend = queryAmmend + " AND year = '" + year + "' ";
            }
        } else if (requestType.split("=")[0].equals("genre")) {
            String genre = requestType.split("=")[1];
            queryAmmend = "AND genres.name = " + "'" + genre + "'";
        } else if (requestType.split("=")[0].equals("prefix")) {
            String prefix = requestType.split("=")[1];
            queryAmmend = "AND title LIKE '" + prefix + "%'";
        }

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("MovieList Connection established!\n");
            Statement statement = connection.createStatement();

            // if request type is sort, then use the pre-existing sql query
            if (requestType.equals("sort")) {
                sortBy = request.getParameter("sort-by");
                pageSize = request.getParameter("page-size");

                // fetch the previous sql query using session
                HttpSession session = request.getSession();
                ArrayList<String> queryHistory = (ArrayList<String>) session.getAttribute("queryHistory");
                query = queryHistory.get(0);
                query += " " + sortBy + " " + pageSize;
                System.out.println("query with sort: " + query);

            } else {
                //----------create a session object----------------
                // clear previous cached sql session query since we are starting a new request-type=search/browse
                HttpSession session = request.getSession();
                ArrayList<String> queryHistory = (ArrayList<String>) session.getAttribute("queryHistory");
                if (queryHistory == null){
                    queryHistory = new ArrayList<>();
                }
                else {
                    queryHistory.clear();
                    session.setAttribute("queryHistory", queryHistory);
                }
                // add the search/browse portion to the query
                query += queryAmmend;
                // Add the sql query to the session queryHistory arrayList
                queryHistory.add(query);
                // set it in the session
                session.setAttribute("queryHistory", queryHistory);
                // print out to debug
                System.out.println("size of arraylist: " + queryHistory.size());
                for (int i = 0; i < queryHistory.size(); i++){
                    System.out.println("queryHistory: " + queryHistory.get(i));
                }

                // add default sort and page number 
                query += sortBy + pageSize;
                System.out.println("default query: " + query);
            }

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
                String movie_star_IDs = "";

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
                ResultSet resultStars = statementStars.executeQuery(queryStars);
                while (resultStars.next()) {
                    movie_stars += resultStars.getString("name") + ", ";
                }

                Statement statementStarIDs = connection.createStatement();
                String queryStarIDs = "SELECT stars.id\n" +
                        "FROM movies JOIN stars_in_movies JOIN stars\n" +
                        "WHERE movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id " +
                        "AND movies.id = '" + movie_id + "'\n" +
                        "LIMIT 3;";
                ResultSet resultStarIDs = statementStarIDs.executeQuery(queryStarIDs);
                while (resultStarIDs.next()) {
                    movie_star_IDs += resultStarIDs.getString("id") + ", ";
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
                jsonObject.addProperty("star_id", movie_star_IDs);

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