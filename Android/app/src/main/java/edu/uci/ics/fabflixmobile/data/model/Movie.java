package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String id;
    private final String name;
    private final short year;
    private final String director;
    private final String rating;
    private final String genres;
    private final String stars;

    public Movie(String id, String name, short year, String director, String rating, String genres, String stars) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.rating = rating;
        this.genres = genres;
        this.stars = stars;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public short getYear() {
        return year;
    }
    public String getDirector() { return director; }
    public String getRating() { return rating; }
    public String getGenres() { return genres; }
    public String getStars() { return stars; }
}