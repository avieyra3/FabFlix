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
import java.util.HashMap;


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
        String queryFromClause = "FROM movies JOIN ratings\n ";
        String queryWhereClause = "WHERE movies.id = ratings.movieId\n ";
        String queryAmendJoins = ""; // this is sql string that will include info from search/browse
        String queryAmendConditions = ""; // this is sql string that will include info from search/browse
        String sortBy = " ORDER BY title ASC, rating ASC"; // the default sort setting
        Integer pageSize = 10; // the default page size
        Integer pageNumber = 0;

        if (requestType == null) {
            ;
        } else if (requestType.equals("search")) {
            session.setAttribute("request-type", "search");
            HashMap<String, String> searchMap = new HashMap<>();
            queryAmendJoins = "JOIN stars_in_movies JOIN stars\n";
            queryAmendConditions = "AND movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id " +
                    "AND title LIKE ? AND director LIKE ? AND stars.name LIKE ?";

            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String star = request.getParameter("star");

            System.out.println(title + " " + year + " " + director + " " + star);

            searchMap.put("title", "%" + title + "%");
            searchMap.put("director", "%" + director + "%");
            searchMap.put("star", "%" + star + "%");

            if (year != "")
                queryAmendConditions += " AND year = ?\n";

            searchMap.put("year", year);
            session.setAttribute("search", searchMap);

        } else if (requestType.split("=")[0].equals("genre")) {
            session.setAttribute("request-type", "genre");
            String genre = requestType.split("=")[1];
            HashMap<String, String> genreMap = new HashMap<>();
            genreMap.put("genre", genre);
            session.setAttribute("genre", genreMap);

            queryAmendJoins = "JOIN genres_in_movies JOIN genres\n";
            queryAmendConditions = "AND movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id\n" +
                    "AND genres.name = ?\n";

        } else if (requestType.split("=")[0].equals("prefix")) {
            session.setAttribute("request-type", "prefix");
            String prefix = requestType.split("=")[1];
            HashMap<String, String> prefixMap = new HashMap<>();

            if (prefix.equals("*")) {
                prefixMap.put("prefix", "^[^a-zA-Z0-9]");
                queryAmendConditions = "AND title REGEXP ?\n";

            } else {
                prefixMap.put("prefix", prefix + "%");
                queryAmendConditions = "AND title LIKE ?\n";
            }
        }
        PrintWriter out = response.getWriter();
        System.out.println("query: " + query);
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("MovieList Connection established!\n");
            Statement statement = connection.createStatement();

            if (requestType.equals("next")) {
                session.setAttribute("sub-request", "next");
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

                // fetch the previous sql query using session
                String queryHistory = (String) session.getAttribute("queryHistory");
                query = queryHistory + " ? LIMIT ? OFFSET ?";
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
                if (restoreQuery != null && restoreQuery != "") {
                    query = restoreQuery;
                }
            } else {
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
                // assigns session queryHistory string to the new query
                queryHistory = query;
                // set it in the session
                session.setAttribute("queryHistory", queryHistory);
                // print out to debug
                System.out.println("queryHistory: " + queryHistory);

                // add default sort and page number
                query += " ORDER BY title ASC, rating ASC LIMIT 10;";
            }

            // save query in case user directs to single move/star page
            session.setAttribute("restored-query", query);
            System.out.println("query: " + query);

            PreparedStatement preparedQuery = connection.prepareStatement(query);
            //sets value for the placeholders for execution
            updateStatement(preparedQuery, session);
            ResultSet result = preparedQuery.executeQuery();
            System.out.println("result statement: " + result);

            JsonArray jsonArray = new JsonArray();
            Integer totalResults = 0;
            while (result.next()) {
                totalResults += 1;
                String movie_id = result.getString("id");
                String movie_title = result.getString("title");
                String movie_year = result.getString("year");
                String movie_director = result.getString("director");
                String movie_rating = result.getString("rating");
                String movie_genres = concatGenres(connection, movie_id);
                String[] starsNstarsID = concatStarsNId(connection, movie_id);
                String movie_stars = starsNstarsID[0];
                String movie_star_IDs = starsNstarsID[1];

                System.out.println("movie id: " + movie_id + " title: " + movie_title + " year: " + movie_year +
                        " director: " + movie_director + " rating: " + movie_rating + " genres: " + movie_genres +
                        " stars: " + movie_stars);

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
    protected void updateStatement(PreparedStatement statement, HttpSession session) throws SQLException{

        if (session.getAttribute("request-type").equals("search"))
        {
            HashMap<String, String> searchData = (HashMap<String, String>) session.getAttribute("search");
            statement.setString(1, searchData.get("title"));
            statement.setString(2, searchData.get("director"));
            statement.setString(3, searchData.get("star"));
            if (!searchData.get("year").equals(""))
                statement.setString(4, searchData.get("year"));
        }
        if(session.getAttribute("request-type").equals("genre")) {
            HashMap<String, String> browseData = (HashMap<String, String>) session.getAttribute("genre");
            statement.setString(1, browseData.get("genre"));
        }
        if (session.getAttribute("request-type").equals("prefix")) {
            HashMap<String, String> prefixData = (HashMap<String, String>) session.getAttribute("prefix");
            statement.setString(1, prefixData.get("prefix"));
        }
//        if (session.getAttribute("sub-request").equals("next")) {
//            statement.setString(index_placeholder + 1, (String) session.getAttribute("sortBy"));
//            Integer pagesize = (Integer) session.getAttribute("pageSize");
//            Integer pagenumber = (Integer) session.getAttribute("pageNumber");
//            statement.setInt(index_placeholder + 2, pagesize); // LIMIT
//            statement.setInt(index_placeholder + 3, pagenumber * pagesize); // OFFSET
//        }
    }
    protected String concatGenres(Connection connection, String movieId) throws SQLException {
        String movie_genres = "";
        String queryGenres = "SELECT genres.name\n" +
                "FROM movies JOIN genres_in_movies JOIN genres\n" +
                "WHERE movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id " +
                "AND movies.id = ?\n" +
                "ORDER BY genres.name LIMIT 3;";
        PreparedStatement statementGenres = connection.prepareStatement(queryGenres);
        statementGenres.setString(1, movieId);
        ResultSet resultGenres = statementGenres.executeQuery();
        while (resultGenres.next()) {
            movie_genres += resultGenres.getString("name") + ", ";
        }
        resultGenres.close();
        statementGenres.close();
        return movie_genres.substring(0, movie_genres.length() - 2);
    }

    protected String[] concatStarsNId(Connection connection, String movie_Id)
            throws SQLException {
        String[] strArr = new String[2];
        String movie_stars = "";
        String movie_star_IDs = "";
        String queryStars = "SELECT stars.name, stars.id, count(*) as movie_counts\n" +
                "FROM movies JOIN stars_in_movies JOIN stars JOIN stars_in_movies as sm2 JOIN movies as m2\n" +
                "WHERE movies.id = stars_in_movies.moviesId AND stars_in_movies.starId = stars.id AND movies.id = ?" +
                " AND stars.id = sm2.starId AND sm2.moviesId = m2.id\n" +
                "GROUP BY stars.id\n" +
                "ORDER BY movie_counts DESC, stars.name ASC LIMIT 3";
        PreparedStatement statementStars = connection.prepareStatement(queryStars);
        statementStars.setString(1, movie_Id);
        ResultSet resultStars = statementStars.executeQuery();
        while (resultStars.next()) {
            movie_stars += resultStars.getString("name") + ", ";
            movie_star_IDs += resultStars.getString("id") + ", ";
        }
        strArr[0] = movie_stars.substring(0, movie_stars.length() - 2);
        strArr[1] = movie_star_IDs.substring(0, movie_star_IDs.length() - 2);
        resultStars.close();
        statementStars.close();
        return strArr;
    }
}