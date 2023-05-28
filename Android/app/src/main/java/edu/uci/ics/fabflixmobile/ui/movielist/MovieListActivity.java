package edu.uci.ics.fabflixmobile.ui.movielist;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
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
import edu.uci.ics.fabflixmobile.databinding.ActivityMovielistBinding;
import edu.uci.ics.fabflixmobile.ui.mainpage.MainActivity;
import edu.uci.ics.fabflixmobile.ui.singlemovie.SingleMovieActivity;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieListActivity extends AppCompatActivity {
    private final String host = "3.130.24.161";
    private final String port = "8443";
    private final String domain = "s23-122b-web_dev";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private TextView pageNum;

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
                    Log.d("movielist.titleRequest.json", response);

                    final StringRequest pageLimitRequest = new StringRequest(
                            Request.Method.GET,
                            baseURL + "/api/movielist?request-type=sort&page-size=20",
                            responseUpdated -> {
                                // TODO: should parse the json response to redirect to appropriate functions
                                //  upon different response value.
                                Log.d("movielist.pageLimitRequest.json", responseUpdated);
                                try {
                                    JSONArray resultData = new JSONArray(responseUpdated);

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
                                Log.d("movielist.pageLimitRequest.error", error.toString());
                            });
                    // important: queue.add is where the login request is actually sent
                    queue.add(pageLimitRequest);
                },
                error -> {
                    // error
                    Log.d("movielist.titleRequest.error", error.toString());
                });
        // important: queue.add is where the login request is actually sent
        queue.add(searchRequest);



        ActivityMovielistBinding binding = ActivityMovielistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final Button prevButton = binding.prev;
        final Button nextButton = binding.next;
        pageNum = binding.pagenum;

        prevButton.setOnClickListener(view -> pagination("prev"));
        nextButton.setOnClickListener(view -> pagination("next"));
    }

    @SuppressLint("SetTextI18n")
    public void pagination(String direction) {
        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movielist?request-type=" + direction,
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
                        pageNum.setText(String.valueOf((Integer) (((JSONObject) resultData.get(0)).get("pageNumber")) + 1));
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