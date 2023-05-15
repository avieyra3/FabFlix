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

@WebServlet(name = "AddMovieServlet", urlPatterns = "/api/add-movie")
public class AddMovieServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("AddMovieServlet doPost EXECUTING");
        response.setContentType("application/json");

        // Get all the data from the request obj
        String title = request.getParameter("title");
        String yearStr = request.getParameter("year");
        Integer year = Integer.parseInt(yearStr);
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        String birthYearStr = request.getParameter("birthYear");
        String genre = request.getParameter("genre");
        Integer birthYear = null;

        // convert birthYear to Integer if not null
        if (birthYearStr != null && !birthYearStr.isEmpty()) {
            try {
                birthYear = Integer.parseInt(birthYearStr);
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Invalid birthYear format. It should be a number.");
                return;
            }
        }
        System.out.println("--------------------------\nmovie:\nTitle: " + title + " Year: " + year +
                " Director: " + director + " starName: " + starName + " birth year: " + birthYear +
                " Genre: " + genre + "\n--------------------------");

        PrintWriter out = response.getWriter();

        try (Connection connection = dataSource.getConnection()) {
            System.out.println("AddMovieServlet Connection established!\n");

            // create call to procedure
            String procedure = "CALL add_movie(?, ?, ?, ?, ?, ?)";
            PreparedStatement addMovie = connection.prepareStatement(procedure);
            // set values for placeholder
            addMovie.setString(1, title);
            addMovie.setInt(2, year);
            addMovie.setString(3, director);
            addMovie.setString(4, starName);
            addMovie.setString(6, genre);

            // Check if birthyear is null
            if (birthYear == null) {
                addMovie.setNull(5, java.sql.Types.INTEGER); // this will set null of type Integer
                System.out.println("null birth year added!");
            }
            else {
                addMovie.setInt(5, birthYear);
                System.out.println("birth year added!");
            }

            // execute query
            int hasResults = addMovie.executeUpdate();
            System.out.println("addMovie query executed!");
            addMovie.close();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("status", "success");
            out.write(jsonObject.toString());
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
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("AddMovieServlet doGet EXECUTING");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        // get the needed names from the POST
        String starName = request.getParameter("starName");
        String genre = request.getParameter("genre");
        System.out.println("getParams: " + starName + " " + genre);
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("AddMovieServlet Connection established!\n");
            String newMovieId = "";
            // Get the newly added movie Id
            String movieIdQuery = "SELECT max(m.id) AS newMovieId FROM movies m";
            PreparedStatement movieIdStatement = connection.prepareStatement(movieIdQuery);
            ResultSet resultMovieId = movieIdStatement.executeQuery();
            System.out.println("movieIdQuery executed!");

            JsonObject jsonObject = new JsonObject();
            while (resultMovieId.next()) {
                newMovieId = resultMovieId.getString("newMovieId");
            }
            movieIdStatement.close();
            resultMovieId.close();

            // Get the POST star id
            String newStarId = "";
            String starIdQuery = "SELECT s.id AS newStarId FROM stars s WHERE s.name = ? LIMIT 1";
            PreparedStatement starIdStatement = connection.prepareStatement(starIdQuery);
            starIdStatement.setString(1, starName);
            ResultSet resultStarId = starIdStatement.executeQuery();
            System.out.println("starIdQuery executed!");
            while (resultStarId.next()) {
                newStarId = resultStarId.getString("newStarId");
            }
            starIdStatement.close();
            resultStarId.close();

            // get the POST genre id
            String newGenreId = "";
            String genreIdQuery = "SELECT g.id AS newGenreId FROM genres g WHERE g.name = ? LIMIT 1";
            PreparedStatement genreIdStatement = connection.prepareStatement(genreIdQuery);
            genreIdStatement.setString(1, genre);
            ResultSet resultGenreId = genreIdStatement.executeQuery();
            System.out.println("movieIdQuery executed!");
            while (resultGenreId.next()) {
                newGenreId = resultGenreId.getString("newGenreId");
            }
            genreIdStatement.close();
            resultGenreId.close();

            System.out.println("\nnewMovieId: " + newMovieId + " newStarId: " + newStarId + " newGenreId: " +
                    newGenreId + "\n");
            jsonObject.addProperty("newMovieId", newMovieId);
            jsonObject.addProperty("newStarId", newStarId);
            jsonObject.addProperty("newGenreId", newGenreId);
            out.write(jsonObject.toString());

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