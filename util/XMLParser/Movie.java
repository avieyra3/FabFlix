import java.util.ArrayList;
import java.util.HashSet;

public class Movie {
    private final String title;
    private final int year;
    private final String director;
    private final HashSet<Star> stars;
    private final ArrayList<String> genres;

    public Movie(String title, int year, String director, HashSet<Star> stars, ArrayList<String> genres) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.stars = stars;
        this.genres = genres;
    }
}
