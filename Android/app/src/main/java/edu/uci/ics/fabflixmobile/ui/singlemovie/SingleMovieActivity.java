package edu.uci.ics.fabflixmobile.ui.singlemovie;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import edu.uci.ics.fabflixmobile.R;
import edu.uci.ics.fabflixmobile.data.NetworkManager;
import edu.uci.ics.fabflixmobile.databinding.ActivitySinglemovieBinding;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SingleMovieActivity extends AppCompatActivity {
    private final String host = "3.130.24.161";
    private final String port = "8443";
    private final String domain = "s23-122b-web_dev";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    private TextView title;
    private TextView year;
    private TextView genres;
    private TextView stars;
    private TextView director;
    private TextView rating;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySinglemovieBinding binding = ActivitySinglemovieBinding.inflate(getLayoutInflater());
        // upon creation, inflate and initialize the layout
        setContentView(binding.getRoot());
        title = binding.title;
        year = binding.year;
        genres = binding.genres;
        stars = binding.stars;
        director = binding.director;
        rating = binding.rating;

        //Get params from MainActivity
        String id = "";
        Bundle param = getIntent().getExtras();
        if (param != null) {
            id = param.getString("id");
        }
        Log.d("singlemovie.id", id);


        // TODO: this should be retrieved from the backend server
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is GET
        final StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + id,
                response -> {
                    // TODO: should parse the json response to redirect to appropriate functions
                    //  upon different response value.
                    Log.d("singlemovie.json", response);
                    try {
                        JSONArray resultData = new JSONArray(response);
                        JSONObject jsonObject = (JSONObject) resultData.get(0);

                        title.setText( (String) jsonObject.get("movie_title"));
                        year.setText( (String) jsonObject.get("movie_year"));
                        director.setText( (String) jsonObject.get("movie_director"));
                        rating.setText( (String) jsonObject.get("movie_rating"));
                        genres.setText(( (String) jsonObject.get("movie_genres")).replace("|", ", "));
                        stars.setText(( (String) jsonObject.get("movie_stars")).replace("|", ", "));

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
