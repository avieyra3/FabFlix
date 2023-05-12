import java.sql.*;
import java.util.*;

public class DomParser {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {

        HashSet<String> movieTitlesFromDB = new HashSet<String>();
        HashMap<String, String> starNamesFromDB = new HashMap<String, String>();
        HashMap<String, Integer> genresFromDB = new HashMap<String, Integer>();

//        MainParser mainParser = new MainParser();
//        mainParser.run();
//
//        ActorParser actorParser = new ActorParser();
//        actorParser.run();
//
        CastParser castParser = new CastParser();
        castParser.run();

        Connection conn = null;

        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement psQueryMovieTitles = null;
        String queryMovieTitles = "SELECT title FROM movies;";
        try {
            psQueryMovieTitles = conn.prepareStatement(queryMovieTitles);
            ResultSet rsMovieTitles = psQueryMovieTitles.executeQuery();
            while (rsMovieTitles.next()) {
                String movieTitle = rsMovieTitles.getString("title");
                movieTitlesFromDB.add(movieTitle);
            }
            //System.out.println(movieTitlesFromDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement psQueryStarNames = null;
        String queryStarNames = "SELECT id, name FROM stars;";
        try {
            psQueryStarNames = conn.prepareStatement(queryStarNames);
            ResultSet rsStarNames = psQueryStarNames.executeQuery();
            while (rsStarNames.next()) {
                String starName = rsStarNames.getString("name");
                String starID = rsStarNames.getString("id");
                starNamesFromDB.put(starName, starID);
            }
            //System.out.println(starNamesFromDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        PreparedStatement psQueryGenres = null;
        String queryGenres = "SELECT id, name FROM genres;";
        try {
            psQueryGenres = conn.prepareStatement(queryGenres);
            ResultSet rsGenres = psQueryGenres.executeQuery();
            while (rsGenres.next()) {
                String genre = rsGenres.getString("name");
                int id = rsGenres.getInt("id");
                genresFromDB.put(genre, id);
            }
            //System.out.println(genresFromDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }





        try {
            if (psQueryMovieTitles!=null) psQueryMovieTitles.close();
            if (conn!=null) conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
