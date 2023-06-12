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
import java.util.Set;
import java.util.HashSet;
import java.io.*;


@WebServlet(name = "MovieListServlet", urlPatterns = "/api/movielist")
public class MovieListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    private static Set<String> stringSet;
    static {
        stringSet = new HashSet<>();
    }

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("\n-------MovieList doGet EXECUTING");
        response.setContentType("application/json");
        System.out.println("request query: " + request.getQueryString());
        String requestType = request.getParameter("request-type");
        System.out.println("request type: " + requestType);
        //----------create a session object----------------
        HttpSession session = request.getSession();

        // These strings are the default/preliminary value before modification
        // this is the data sql query string that will be modified according to search/browse and sort.
        String query = "";
        String querySelectClause = "SELECT DISTINCT m.id, m.title, m.year, m.director, IFNULL(r.rating, 'N/A') AS rating\n"+
                "FROM movies m LEFT JOIN ratings r ON m.id = r.movieId ";
        String queryAmendJoins = ""; // this is sql string that will include info from search/browse
        String queryAmendConditions = "WHERE 1=1 "; // this is sql string that will include info from search/browse
        String sortBy = "ORDER BY title ASC, rating ASC"; // the default sort setting
        Integer pageSize = 10; // the default page size
        Integer pageNumber = 0;
        stringSet = sortSet(); // instantiate the set with the sort associated statements

        if (requestType == null) {
            ;
        } else if (requestType.equals("search")) {
            // set the session request type as search
            session.setAttribute("request-type", "search");
            queryAmendJoins = "JOIN stars_in_movies sim ON m.id = sim.moviesId JOIN stars s ON sim.starId = s.id\n";

            // get the search parameters to identify search conditions
            String title = request.getParameter("title");
            String year = request.getParameter("year");
            String director = request.getParameter("director");
            String starName = request.getParameter("star");
            System.out.println(title + " " + year + " " + director + " " + starName);

            // instantiate session attributes even if the strings are empty. This will make it easier to update the
            // statements later since the attributes at the very least will never be null.
            session.setAttribute("title", title);
            session.setAttribute("year", year);
            session.setAttribute("director", director);
            session.setAttribute("starName", starName);

            // check which parameters are not empty and save inside a session
            if (!title.isEmpty())
                queryAmendConditions += "AND title LIKE ? ";
            if (!year.isEmpty())
                queryAmendConditions += "AND year = ? ";
            if (!director.isEmpty())
                queryAmendConditions += "AND director LIKE ? ";
            if (!starName.isEmpty())
                queryAmendConditions += "AND s.name LIKE ? ";

        } else if (requestType.split("=")[0].equals("genre")) {
            // Set the session request type as genre
            session.setAttribute("request-type", "genre");
            // get the genre value str and set in the session
            String genre = requestType.split("=")[1];
            session.setAttribute("genre", genre);
            // append the needed strings for joins and conditions
            queryAmendJoins = "JOIN genres_in_movies gim ON m.id = gim.movieId JOIN genres g ON gim.genreId = g.id\n";
            queryAmendConditions = "WHERE g.name = ?\n";

        } else if (requestType.split("=")[0].equals("prefix")) {
            //set the session request type as prefix
            session.setAttribute("request-type", "prefix");
            // get the prefix value and add it to the session
            String prefix = requestType.split("=")[1];

            // special case requires that if prefix = * then we use regexp
            if (prefix.equals("*")) {
                prefix = "^[^a-zA-Z0-9]";
                queryAmendConditions = "WHERE title REGEXP ?\n";

            } else
                queryAmendConditions = "WHERE title LIKE ?\n";
            // have to set the wildcard here because of the * condition otherwise,
            // would need to check it in the update
            session.setAttribute("prefix", prefix + '%');

        } else if (requestType.split("=")[0].equals("fts")) {
            // set up session attributes
            String ftsQuery = requestType.split("=")[1];
            String[] tokenQueryArr = ftsQuery.split("\\s+");
            String likeStr = '%' + ftsQuery + '%';
            String ftStr = "";
            int threshold = (int)Math.round(ftsQuery.length() * 0.30);
            String tokens = "Tokens: ";
            for (int i = 0; i < tokenQueryArr.length; i++) {
                tokens += tokenQueryArr[i] + " ";
                ftStr += "+" + tokenQueryArr[i] + "* ";
            }

            // store session data
            session.setAttribute("request-type", "fts");
            session.setAttribute("fuzzy", ftsQuery);
            session.setAttribute("likeStr", likeStr);
            session.setAttribute("threshold", threshold);
            session.setAttribute("ftStr", ftStr);
            // assign query to fts with fuzzy search.
            queryAmendConditions += "AND title LIKE ? OR edth(title, ?, ?) UNION " + querySelectClause +
                    "WHERE MATCH(title) AGAINST ( ? IN BOOLEAN MODE) ";
        }
        // -----------------------------------------------------------------------------------------------------
        // The next set of if statements are actually checking the "sub" request type. So a new chain of if-else
        // starts here verifying whether we need to "add" to the previous query or apply the default setting.
        // -----------------------------------------------------------------------------------------------------
        if (requestType.equals("next")) {
            // set sub request as next
            session.setAttribute("sub-request", "next");
            // We get the sessions data and reconstruct the query to incorporate the offset (new page)
            pageNumber = (Integer) session.getAttribute("pageNumber");
            pageSize = (Integer) session.getAttribute("pageSize");

            // edge case: make sure that the user cannot exceed the max page size. For example, we do not
            // add a page if the number of results in the previous query was less than 10. Of course,
            // we do need to watch out for the case where totalResults is at 10 which this doesnt solve for.
            Integer totalResults = (Integer) session.getAttribute("totalResults"); // gets the total results
            if (totalResults < pageSize) {
                ;
            } else {
                pageNumber += 1;
            }
            session.setAttribute("pageNumber", pageNumber);

            // fetch the previous sql query session and sort parameters
            String queryHistory = (String) session.getAttribute("queryHistory");
            sortBy = (String) session.getAttribute("sortBy");
            query = queryHistory + sortBy + " LIMIT ? OFFSET ?";
            //System.out.println("next query: " + query);

        } else if (requestType.equals("prev")) {
            session.setAttribute("sub-request", "prev");
            pageNumber = (Integer) session.getAttribute("pageNumber");
            // edge case: we cannot have a page less than zero.
            pageNumber -= 1;
            if (pageNumber < 0) {
                pageNumber = 0;
            }
            session.setAttribute("pageNumber", pageNumber);

            // fetch the previous sql query session and sort parameters
            String queryHistory = (String) session.getAttribute("queryHistory");
            sortBy = (String) session.getAttribute("sortBy");
            query = queryHistory + sortBy + " LIMIT ? OFFSET ? ";
            //System.out.println("\nprev query: " + query + "\n");

        } else if (requestType.equals("sort")) {
            // if request type is sort, then use the pre-existing sql query and set sub-request to sort
            // check first if the sortBy parameter is equal to any of the preset to for security measures
            // if the sortBy variable returned does not match any of the strings in stringSet revert to
            // default string
            session.setAttribute("sub-request", "sort");
            sortBy = request.getParameter("sort-by");
            if (!stringSet.contains(sortBy)) {
                sortBy = "ORDER BY title ASC, rating ASC";
                session.setAttribute("sortBy", sortBy);

            } else
                session.setAttribute("sortBy", sortBy);

            pageSize = Integer.parseInt(request.getParameter("page-size"));
            session.setAttribute("pageSize", pageSize);

            // fetch the previous sql query using session
            String queryHistory = (String) session.getAttribute("queryHistory");
            query = queryHistory + sortBy + " LIMIT ? OFFSET ?";
            //System.out.println("\nquery with sort: " + query + "\n");

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
            session.setAttribute("sub-request", "");

            // add the search/browse portion to the query
            query = querySelectClause + queryAmendJoins + queryAmendConditions;
            // assigns session queryHistory string to the new query
            queryHistory = query;
            // set it in the session
            session.setAttribute("queryHistory", queryHistory);
            // print out to debug
            //System.out.println("queryHistory: " + queryHistory);

            // add default sort and page number
            query += " ORDER BY title ASC, rating ASC LIMIT 10";
        }
        PrintWriter out = response.getWriter();
        //System.out.println("query: " + query);
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("MovieList Connection established!\n");
            Statement statement = connection.createStatement();

            // save query in case user directs to single move/star page
            session.setAttribute("restored-query", query);
            System.out.println("query: " + query);

            PreparedStatement preparedQuery = connection.prepareStatement(query);
            //sets value for the placeholders for execution
            updateStatement(preparedQuery, session);

            // ----start of log time ------
            long startTime = System.currentTimeMillis();

            ResultSet result = preparedQuery.executeQuery();

            // ----end of log time -------
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;

            JsonArray jsonArray = new JsonArray();
            Integer totalResults = 0;
            while (result.next()) {
                totalResults += 1;
                String movie_id = result.getString("id");
                String movie_title = result.getString("title");
                String movie_year = result.getString("year");
                String movie_director = result.getString("director");
                String movie_rating = result.getString("rating");

                // sum elapsed time with the request session attribute
                String movie_genres = concatGenres(connection, movie_id, request);
                elapsedTime += (long) request.getAttribute("TJ");
                String[] starsNstarsID = concatStarsNId(connection, movie_id, request);
                elapsedTime += (long) request.getAttribute("TJ");

                String movie_stars = starsNstarsID[0];
                String movie_star_IDs = starsNstarsID[1];

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
                System.out.println("Response JSON Object: " + jsonObject);
                jsonArray.add(jsonObject);
            }
            session.setAttribute("totalResults", totalResults);
            result.close();
            statement.close();

            request.getServletContext().log("getting " + jsonArray.size() + " results");
            request.setAttribute("TJ", elapsedTime);
            out.write(jsonArray.toString());
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
        System.out.println("-------MovieListServlet doGet Done!\n");
    }
    protected void updateStatement(PreparedStatement statement, HttpSession session) throws SQLException{
        int paramIndex = 1;
        if (session.getAttribute("request-type").equals("search"))
        {
            // get back the respect attributes
            String title = (String) session.getAttribute("title");
            String director = (String) session.getAttribute("director");
            String starName = (String) session.getAttribute("starName");
            String yearStr = (String) session.getAttribute("year");
            System.out.println("placeholder values: " + title + " " + yearStr + " " + director + " " + starName);
            // update the placeholders
            if (!title.isEmpty())
                statement.setString(paramIndex++, '%' + title + '%');
            if (!yearStr.isEmpty()) {
                int year = Integer.parseInt(yearStr);
                statement.setInt(paramIndex++, year);
            }
            if (!director.isEmpty())
                statement.setString(paramIndex++, '%' + director + '%');
            if(!starName.isEmpty())
                statement.setString(paramIndex++, '%' + starName + '%');
        }
        if(session.getAttribute("request-type").equals("genre")) {
            System.out.println("placeholder values: " + (String) session.getAttribute("genre"));
            statement.setString(paramIndex++, (String) session.getAttribute("genre"));
        }
        if (session.getAttribute("request-type").equals("prefix")) {
            System.out.println("placeholder values: " + (String) session.getAttribute("prefix"));
            statement.setString(paramIndex++, (String) session.getAttribute("prefix"));
        }
        if (session.getAttribute("request-type").equals("fts")) {
            // fetch the url query str and assign to urlQuery (i.e. request-type=fts="the string we are fetching")
            String ftStr = (String) session.getAttribute("ftStr");
            String likeStr = (String) session.getAttribute("likeStr");
            int threshold = (Integer) session.getAttribute("threshold");
            String fuzzy = (String) session.getAttribute("fuzzy");
            System.out.println("ftStr: " + ftStr + "\nlikeStr: " + likeStr);

            // update values
            statement.setString(paramIndex++, likeStr);
            statement.setString(paramIndex++, fuzzy);
            statement.setInt(paramIndex++, threshold);
            statement.setString(paramIndex++, ftStr);
        }
        // check if there was a sub request
        String subRequest = (String) session.getAttribute("sub-request");
        if (subRequest.equals("next") || subRequest.equals("prev") || subRequest.equals("sort")) {
            //System.out.println("\nSub Request in prog....\n");
            int psize = (Integer) session.getAttribute("pageSize");
            int pnum = (Integer) session.getAttribute("pageNumber");
            int offset = psize * pnum;
            statement.setInt(paramIndex++, psize); // LIMIT
            statement.setInt(paramIndex++, offset); // OFFSET
            System.out.println("placeholder values: " + psize + " " + offset);
        }
    }
    protected String concatGenres(Connection connection, String movieId, HttpServletRequest request) throws SQLException {
        String movie_genres = "";
        String queryGenres = "SELECT genres.name\n" +
                "FROM movies JOIN genres_in_movies JOIN genres\n" +
                "WHERE movies.id = genres_in_movies.movieId AND genres_in_movies.genreId = genres.id " +
                "AND movies.id = ?\n" +
                "ORDER BY genres.name LIMIT 3;";
        PreparedStatement statementGenres = connection.prepareStatement(queryGenres);
        statementGenres.setString(1, movieId);

        // ----start of log time ------
        long startTime = System.currentTimeMillis();

        ResultSet resultGenres = statementGenres.executeQuery();

        // ----end of log time -------
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        request.setAttribute("TJ", elapsedTime);
        System.out.println("concatGenres TJ time: " + elapsedTime);
        while (resultGenres.next()) {
            movie_genres += resultGenres.getString("name") + ", ";
        }
        resultGenres.close();
        statementGenres.close();
        return movie_genres.substring(0, Math.max(0, movie_genres.length() - 2));
    }

    protected String[] concatStarsNId(Connection connection, String movie_Id, HttpServletRequest request)
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

        // ----start of log time ------
        long startTime = System.currentTimeMillis();

        ResultSet resultStars = statementStars.executeQuery();

        // ----end of log time -------
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        System.out.println("concatStars tj time: " + elapsedTime);
        request.setAttribute("TJ", elapsedTime);

        while (resultStars.next()) {
            movie_stars += resultStars.getString("name") + ", ";
            movie_star_IDs += resultStars.getString("id") + ", ";
        }
        strArr[0] = movie_stars.substring(0, Math.max(0, movie_stars.length() - 2));
        strArr[1] = movie_star_IDs.substring(0, Math.max(0, movie_star_IDs.length() - 2));
        resultStars.close();
        statementStars.close();
        return strArr;
    }

    private HashSet<String> sortSet() {
        HashSet<String> paramSet = new HashSet<>();
        paramSet.add("ORDER BY title ASC, rating ASC");
        paramSet.add("ORDER BY title ASC, rating DESC");
        paramSet.add("ORDER BY title DESC, rating ASC");
        paramSet.add("ORDER BY title DESC, rating DESC");
        paramSet.add("ORDER BY rating ASC, title ASC");
        paramSet.add("ORDER BY rating ASC, title DESC");
        paramSet.add("ORDER BY rating DESC, title ASC");
        paramSet.add("ORDER BY rating DESC, title DESC");
        return paramSet;
    }
}