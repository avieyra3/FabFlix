import java.sql.*;
import java.util.*;

public class DomParser {
    public static void main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        System.out.println("DomParser Running:");
        MainParser mainParser = new MainParser();
        mainParser.run();
        HashMap<String, ArrayList> dataForMoviesTable = mainParser.getDataForMoviesTable();
        ArrayList<ArrayList> dataForGenresInMoviesTable = mainParser.getDataForGenresInMoviesTable();
        HashSet<String> dataForGenresTable = mainParser.getDataForGenresTable();

        ActorParser actorParser = new ActorParser();
        actorParser.run();
        HashMap<String, Integer> dataForStarsTable = actorParser.getDataForStarsTable();

        CastParser castParser = new CastParser();
        castParser.receiveDataForMoviesTable(dataForMoviesTable);
        castParser.receiveDataForStarsTable(dataForStarsTable);
        castParser.run();
        ArrayList<ArrayList> dataForStarsInMoviesTable = castParser.getDataForStarsInMoviesTable();

        Connection conn = null;

        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        String jdbcURL="jdbc:mysql://localhost:3306/moviedb";

        try {
            conn = DriverManager.getConnection(jdbcURL,"mytestuser", "My6$Password");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get movies from database to check for duplication before insert
        HashMap<String, String> movieTitlesFromDB = new HashMap<String, String>(); // Values will not be null
        PreparedStatement psQueryMovieTitles = null;
        String queryMovieTitles = "SELECT id, title FROM movies;";
        try {
            psQueryMovieTitles = conn.prepareStatement(queryMovieTitles);
            ResultSet rsMovieTitles = psQueryMovieTitles.executeQuery();
            while (rsMovieTitles.next()) {
                String movieTitle = rsMovieTitles.getString("title");
                String movieID = rsMovieTitles.getString("id");
                movieTitlesFromDB.put(movieTitle, movieID);
            }
            //System.out.println(movieTitlesFromOldDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get stars from database to check for duplication before insert
        HashMap<String, String> starNamesFromDB = new HashMap<String, String>(); // Values will not be null
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
            //System.out.println(starNamesFromOldDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get genres from database to check for duplication before insert
        HashMap<String, Integer> genresFromDB = new HashMap<String, Integer>(); // Values will not be null
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
            //System.out.println(genresFromOldDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new movies into database
        PreparedStatement psInsertMovies = null;
        String insertMovies = "INSERT INTO movies VALUES(?,?,?,?);";
        try {
            conn.setAutoCommit(false);
            psInsertMovies = conn.prepareStatement(insertMovies);
            int idTail = 0;
            System.out.println("Insert into movies table...");
            int moviesInserted = 0;
            int moviesNoTitle = 0;
            int moviesDuplicate = 0;
            for (Map.Entry<String, ArrayList> row : dataForMoviesTable.entrySet()) {
                String id = "bb" + String.format("%08d", idTail);
                String movieTitle = row.getKey();
                Integer year = (Integer) row.getValue().get(0);
                String director = (String) row.getValue().get(1);
                if (movieTitle != null && !movieTitlesFromDB.containsKey(movieTitle)) {
                    psInsertMovies.setString(1, id);
                    psInsertMovies.setString(2, movieTitle);
                    if (year != null)
                        psInsertMovies.setInt(3, year);
                    else
                        psInsertMovies.setInt(3, 0000);
                    if (director != null)
                        psInsertMovies.setString(4, director);
                    else
                        psInsertMovies.setString(4, "");
                    psInsertMovies.addBatch();
                    idTail++;
                    moviesInserted++;
                } else if (movieTitle == null) {
                    //System.out.println("- " + movieTitle + "/" + year + "/" + director);
                    //System.out.println("  - This movie's title is NULL. No add");
                    moviesNoTitle++;
                } else {
                    //System.out.println("- " + movieTitle + "/" + year + "/" + director);
                    //System.out.println("  - This movie is a duplicate. No add");
                    moviesDuplicate++;
                }
            }
            psInsertMovies.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Inserted " + moviesInserted + " movies. Rejected " + moviesNoTitle + " no titles, "
                    + moviesDuplicate + " duplicates.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new stars into database
        PreparedStatement psInsertStars = null;
        String insertStars = "INSERT INTO stars (id, name, birthYear) VALUES(?,?,?);";
        try {
            conn.setAutoCommit(false);
            psInsertStars = conn.prepareStatement(insertStars);
            int idTail = 0;
            System.out.println("Insert into stars table...");
            int starsInserted = 0;
            int starsNoName = 0;
            int starsDuplicate = 0;
            for (Map.Entry<String, Integer> row : dataForStarsTable.entrySet()) {
                String id = "aa" + String.format("%08d", idTail);
                String starName = row.getKey();
                Integer starBirthYear = row.getValue();
                if (starName != null && !starNamesFromDB.containsKey(starName)) {
                    psInsertStars.setString(1, id);
                    psInsertStars.setString(2, starName);
                    if (starBirthYear != null)
                        psInsertStars.setInt(3, starBirthYear);
                    else
                        psInsertStars.setNull(3, Types.INTEGER);
                    psInsertStars.addBatch();
                    idTail++;
                    starsInserted++;
                } else if (starName == null) {
                    //System.out.println("- " + starName + "/" + starBirthYear);
                    //System.out.println("  - This star's name is NULL. No add");
                    starsNoName++;
                } else {
                    //System.out.println("- " + starName + "/" + starBirthYear);
                    //System.out.println("  - This star is a duplicate. No add");
                    starsDuplicate++;
                }
            }
            psInsertStars.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Inserted " + starsInserted + " stars. Rejected " + starsNoName + " no names, "
                    + starsDuplicate + " duplicates.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new genres into database
        PreparedStatement psInsertGenres = null;
        String insertGenres = "INSERT INTO genres VALUES(NULL,?);";
        try {
            conn.setAutoCommit(false);
            psInsertGenres = conn.prepareStatement(insertGenres);
            System.out.println("Insert into genres table...");
            int genresInserted = 0;
            int genresNull = 0;
            int genresDuplicate = 0;
            for (String row : dataForGenresTable) {
                if (row != null && !genresFromDB.containsKey(row)) {
                    psInsertGenres.setString(1, row);
                    psInsertGenres.addBatch();
                    genresInserted++;
                } else if (row == null) {
                    //System.out.println("  - This genre is NULL. No add");
                    genresNull++;
                } else {
                    //System.out.println("  - This genre is a duplicate. No add");
                    genresDuplicate++;
                }
            }
            psInsertGenres.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Inserted " + genresInserted + " genres. Rejected " + genresNull + " nulls, "
                    + genresDuplicate + " duplicates.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get movies from database with new movies inserted for fast movie ID retrieval
        movieTitlesFromDB = new HashMap<String, String>(); // Values will not be null
        psQueryMovieTitles = null;
        queryMovieTitles = "SELECT id, title FROM movies;";
        try {
            psQueryMovieTitles = conn.prepareStatement(queryMovieTitles);
            ResultSet rsMovieTitles = psQueryMovieTitles.executeQuery();
            while (rsMovieTitles.next()) {
                String movieTitle = rsMovieTitles.getString("title");
                String movieID = rsMovieTitles.getString("id");
                movieTitlesFromDB.put(movieTitle, movieID);
            }
            //System.out.println(movieTitlesFromNewDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get stars from database with new stars inserted for fast star ID retrieval
        starNamesFromDB = new HashMap<String, String>(); // Values will not be null
        psQueryStarNames = null;
        queryStarNames = "SELECT id, name FROM stars;";
        try {
            psQueryStarNames = conn.prepareStatement(queryStarNames);
            ResultSet rsStarNames = psQueryStarNames.executeQuery();
            while (rsStarNames.next()) {
                String starName = rsStarNames.getString("name");
                String starID = rsStarNames.getString("id");
                starNamesFromDB.put(starName, starID);
            }
            //System.out.println(starNamesFromNewDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get genres from database with new genres inserted for fast genre ID retrieval
        genresFromDB = new HashMap<String, Integer>(); // Values will not be null
        psQueryGenres = null;
        queryGenres = "SELECT id, name FROM genres;";
        try {
            psQueryGenres = conn.prepareStatement(queryGenres);
            ResultSet rsGenres = psQueryGenres.executeQuery();
            while (rsGenres.next()) {
                String genre = rsGenres.getString("name");
                int id = rsGenres.getInt("id");
                genresFromDB.put(genre, id);
            }
            //System.out.println(genresFromNewDB.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new star-movie relationships into database
        PreparedStatement psInsertStarsInMovies = null;
        String insertStarsInMovies = "INSERT INTO stars_in_movies VALUES(?,?);";
        try {
            conn.setAutoCommit(false);
            psInsertStarsInMovies = conn.prepareStatement(insertStarsInMovies);
            System.out.println("Insert into stars_in_movies table...");
            int SIMsInserted = 0;
            int SIMsNull = 0;
            for (ArrayList<Object> row : dataForStarsInMoviesTable) {
                String movieTitle = (String) row.get(0);
                String starName = (String) row.get(1);
                if (movieTitle != null && starName != null ) { //&& !movieTitlesFromOldDB.containsKey(movieTitle)
                    String movieID = movieTitlesFromDB.get(movieTitle);
                    String starID = starNamesFromDB.get(starName); //THIS is somehow null
                    psInsertStarsInMovies.setString(1, starID);
                    psInsertStarsInMovies.setString(2, movieID);
                    psInsertStarsInMovies.addBatch();
                    SIMsInserted++;
                } else {
                    //System.out.println("- " + movieTitle + "/" + starName);
                    //System.out.println("  - Movie title or star name is NULL. No add");
                    SIMsNull++;
                }
            }
            psInsertStarsInMovies.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Inserted " + SIMsInserted + " stars_in_movies. Rejected " + SIMsNull + " nulls.");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Insert new genre-movie relationships into database
        PreparedStatement psInsertGenresInMovies = null;
        String insertGenresInMovies = "INSERT INTO genres_in_movies VALUES(?,?);";
        try {
            conn.setAutoCommit(false);
            psInsertGenresInMovies = conn.prepareStatement(insertGenresInMovies);
            System.out.println("Insert into genres_in_movies table...");
            int GIMsInserted = 0;
            int GIMsNull = 0;
            for (ArrayList<Object> row : dataForGenresInMoviesTable) {
                String movieTitle = (String) row.get(0);
                String genre = (String) row.get(1);
                if (movieTitle != null && genre != null ) { //&& !movieTitlesFromOldDB.containsKey(movieTitle)
                    String movieID = movieTitlesFromDB.get(movieTitle);
                    int genreID = genresFromDB.get(genre);
                    psInsertGenresInMovies.setInt(1, genreID);
                    psInsertGenresInMovies.setString(2, movieID);
                    psInsertGenresInMovies.addBatch();
                    GIMsInserted++;
                } else {
                    //System.out.println("- " + movieTitle + "/" + genre);
                    //System.out.println("  - Movie title or genre is NULL. No add");
                    GIMsNull++;
                }
            }
            psInsertGenresInMovies.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Inserted " + GIMsInserted + " genres_in_movies. Rejected " + GIMsNull + " nulls.");
        } catch (SQLException e) {
            e.printStackTrace();
        }




        try {
            psQueryMovieTitles.close();
            psQueryStarNames.close();
            psQueryGenres.close();
            psInsertMovies.close();
            psInsertStars.close();
            psInsertGenres.close();
            psInsertStarsInMovies.close();
            psInsertGenresInMovies.close();
            conn.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
