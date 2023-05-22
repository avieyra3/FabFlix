package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.data.model.Movie;
import edu.uci.ics.fabflixmobile.ui.login.LoginActivity;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "10.0.2.2";
    private final String port = "8443";
    private final String domain = "s23_122b_web_dev_war";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get params from MainActivity
        String ftTitle = "";
        Bundle param = getIntent().getExtras();
        if (param != null) {
            ftTitle = param.getString("title");
        }
        Log.d("movielist.search.title", ftTitle);

        setContentView(R.layout.activity_movielist);

        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movielist?request-type=search&title=" + ftTitle + "&year=&director=&star=",
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("movielist.json", response);
                    try {
                        JSONArray resultData = new JSONArray(response);

                        final ArrayList<Movie> movies = new ArrayList<>();
                        for (int i = 0; i < resultData.length(); i++) {
                            JSONObject jsonObject = (JSONObject) resultData.get(i);
                            String id = (String) jsonObject.get("movie_id");
                            String title = (String) jsonObject.get("movie_title");
                            int year =  jsonObject.getInt("movie_year");
                            String director = (String) jsonObject.get("movie_director");
                            String rating = (String) jsonObject.get("movie_rating");
                            String genres = (String) jsonObject.get("movie_genres");
                            String stars = (String) jsonObject.get("movie_stars");
                            movies.add(new Movie(id, title, (short) year, director, rating, genres, stars));
                        }
                        MovieListViewAdapter adapter = new MovieListViewAdapter(this, movies);
                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            @SuppressLint("DefaultLocale") String message = String.format(
                                    "Clicked on position: %d, name: %s, %d",
                                    position,
                                    movie.getName(),
                                    movie.getYear()
                            );
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                            Intent SingleMoviePage = new Intent(MovieListActivity.this, SingleMovieActivity.class);
                            SingleMoviePage.putExtra("id", movie.getId());
                            startActivity(SingleMoviePage);
                        });
                    } catch (JSONException e) {
                        Log.d("JSON parsing failed", e.getStackTrace().toString());
                    }
                },
                error -> {
                    // error
                    Log.d("movielist.error", error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);
    }
}