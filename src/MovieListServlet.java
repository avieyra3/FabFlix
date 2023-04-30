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
        //----------create a session object----------------
        HttpSession session = request.getSession();

        // These strings are the default/preliminary value before modification
        // this is the data sql query string that will be modified according to search/browse and sort.
        String query = "";
        String querySelectClause = "SELECT DISTINCT movies.id, movies.title, movies.year, movies.director, rating\n";
        String queryFromClause = "FROM movies JOIN ratings\n";
        String queryWhereClause = "WHERE movies.id = ratings.movieId\n";
        String queryAmendJoins = ""; // this is sql string that will include info from search/browse
        String queryAmendConditions = ""; // this is sql string that will include info from search/browse
        String sortBy = " ORDER BY title ASC, rating ASC"; // the default sort setting
        Integer pageSize = 10; // the default page size
        Integer pageNumber = 0;
        if (requestType == null) {
            ;
        } else if (requestType.equals("search")) {
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");

            System.out.println(title + " " + year + " " + director + " " + star);

            queryAmendJoins = "JOIN stars_in_movies JOIN stars\n";
            queryAmendConditions = "AND movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id\n";
            if (title != "")
                queryAmendConditions += "AND title LIKE '%" + title + "%' ";
            if (director != "")
                queryAmendConditions += "AND director LIKE '%" + director + "%' ";
            if (star != "")
                queryAmendConditions += "AND stars.name LIKE '%" + star + "%' ";
            if (year != "")
                queryAmendConditions += "AND year = '" + year + "' ";
            queryAmendConditions += "\n";
        } else if (requestType.split("=")[0].equals("genre")) {
            String genre = requestType.split("=")[1];

            queryAmendJoins = "JOIN genres_in_movies JOIN genres\n";
            queryAmendConditions = "AND movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id\n" +
                                    "AND genres.name = " + "'" + genre + "'\n";
        } else if (requestType.split("=")[0].equals("prefix")) {
            String prefix = requestType.split("=")[1];
            queryAmendConditions = "AND title LIKE '" + prefix + "%'\n";
        }

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("MovieList Connection established!\n");
            Statement statement = connection.createStatement();

            if (requestType.equals("next")) {
                // We get the sessions data and reconstruct the query to incorporate the offset (new page)
                pageNumber = (Integer) session.getAttribute("pageNumber");
                pageSize = (Integer) session.getAttribute("pageSize");

                // edge case: make sure that the user cannot exceed the max page size.
                Integer totalResults = (Integer) session.getAttribute("totalResults"); // gets the total results
                if (totalResults < pageSize) {
                    ;
                } else {
                    pageNumber += 1;
                }
                session.setAttribute("pageNumber", pageNumber);

                sortBy = (String) session.getAttribute("sortBy");
                //calculate offset
                int offset = pageSize * pageNumber;

                // fetch the previous sql query using session
                String queryHistory = (String) session.getAttribute("queryHistory");
                query = queryHistory + sortBy + " LIMIT " + pageSize + " OFFSET " + offset;
                System.out.println("next query: " + query);

            } else if (requestType.equals("prev")) {
                pageNumber = (Integer) session.getAttribute("pageNumber");
                // edge case: we cannot have a page less than zero. 
                pageNumber -= 1;
                if (pageNumber < 0) {
                    pageNumber = 0;
                }
                session.setAttribute("pageNumber", pageNumber);
                pageSize = (Integer) session.getAttribute("pageSize");
                sortBy = (String) session.getAttribute("sortBy");
                //calculate offset
                int offset = pageSize * pageNumber;

                // fetch the previous sql query using session
                String queryHistory = (String) session.getAttribute("queryHistory");
                query = queryHistory + sortBy + " LIMIT " + pageSize + " OFFSET " + offset;
                System.out.println("next query: " + query);

            } else if (requestType.equals("sort")) { // if request type is sort, then use the pre-existing sql query
                sortBy = request.getParameter("sort-by");
                pageSize = Integer.parseInt(request.getParameter("page-size"));
                session.setAttribute("pageSize", pageSize);
                session.setAttribute("sortBy", sortBy);
                pageNumber = (Integer) session.getAttribute("pageNumber");

                // fetch the previous sql query using session
                String queryHistory = (String) session.getAttribute("queryHistory");
                query = queryHistory;
                query += " " + sortBy + " LIMIT " + pageSize + " OFFSET " + (pageSize * pageNumber);
                System.out.println("query with sort: " + query);

            } else if (requestType.split("=")[0].equals("restore")) {

                String restoreQuery = (String) session.getAttribute("restored-query");
                // check if the user somehow went straight to a single movie page, if so, this
                // safety net will still load a list of movies although this technically shouldn't
                // happen.
                if (restoreQuery != null && restoreQuery !="") {
                    query = restoreQuery;
                }
            }else {
                // clear previous cached sql session since we are starting a new request-type=search/browse
                // this means default initializing the variable data
                String queryHistory = "";
                sortBy = "ORDER BY title ASC, rating ASC";
                pageSize = 10;
                pageNumber = 0;
                session.setAttribute("queryHistory", queryHistory);
                session.setAttribute("pageSize", pageSize);
                session.setAttribute("pageNumber", pageNumber);
                session.setAttribute("sortBy", sortBy);
                // add the search/browse portion to the query
                query = querySelectClause + queryFromClause + queryAmendJoins + queryWhereClause + queryAmendConditions;
                // assign the new sql query to the session queryHistory string
                queryHistory = query;
                // set it in the session
                session.setAttribute("queryHistory", queryHistory);
                // print out to debug
                System.out.println("queryHistory: " + queryHistory);

                // add default sort and page number 
                query += sortBy + " LIMIT " + pageSize;
                System.out.println("default query: " + query);
            }
            // save query in case user directs to single move/star page
            session.setAttribute("restored-query", query);
            
            // returns the executed query
            ResultSet result = statement.executeQuery(query);
            JsonArray jsonArray = new JsonArray();
            Integer totalResults = 0;
            while (result.next()) {
                totalResults += 1;
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
                        "ORDER BY genres.name LIMIT 3;";
                ResultSet resultGenres = statementGenres.executeQuery(queryGenres);
                while (resultGenres.next()) {
                    movie_genres += resultGenres.getString("name") + ", ";
                }
                movie_genres = movie_genres.substring(0, movie_genres.length() - 2);

                Statement statementStars = connection.createStatement();
                String queryStars = "SELECT stars.name, stars.id, count(*) as movie_counts\n" +
                        "FROM movies JOIN stars_in_movies JOIN stars JOIN stars_in_movies as sm2 JOIN movies as m2\n" +
                        "WHERE movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id AND movies.id = '"
                        + movie_id + "' AND stars.id = sm2.starId AND sm2.moviesId = m2.id\n" +
                        "GROUP BY stars.id\n" +
                        "ORDER BY movie_counts DESC, stars.name ASC LIMIT 3";
                ResultSet resultStars = statementStars.executeQuery(queryStars);
                while (resultStars.next()) {
                    movie_stars += resultStars.getString("name") + ", ";
                    movie_star_IDs += resultStars.getString("id") + ", ";
                }
                movie_stars = movie_stars.substring(0, movie_stars.length() - 2);
                movie_star_IDs = movie_star_IDs.substring(0, movie_star_IDs.length() - 2);

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
                jsonObject.addProperty("pageNumber", (Integer) session.getAttribute("pageNumber"));
                System.out.println(jsonObject);
                jsonArray.add(jsonObject);

                resultGenres.close();
                resultStars.close();
                statementGenres.close();
                statementStars.close();
            }
            session.setAttribute("totalResults", totalResults);
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