import java.util.ArrayList;
import java.util.HashSet;

public class Movie {
    private final String title;
    private final int year;
    private final String director;
    private final ArrayList<String> genres;

    public Movie(String title, int year, String director, HashSet<Star> stars, ArrayList<String> genres) {
        this.title = title;
        this.year = year;
        this.director = director;
        this.genres = genres;
    }

    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getDirector() { return director; }

    public String toString() {

        return "Title:" + getTitle() + ", " +
                "Year:" + getYear() + ", " +
                "Director:" + getDirector() + ", ";
    }
}
