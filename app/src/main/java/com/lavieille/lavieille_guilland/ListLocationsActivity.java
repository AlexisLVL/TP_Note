package com.lavieille.lavieille_guilland;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.lavieille.lavieille_guilland.adapter.LocationsAdapter;
import com.lavieille.lavieille_guilland.entity.Location;
import com.lavieille.lavieille_guilland.favorite.DBFavorites;
import com.lavieille.lavieille_guilland.signin.FirebaseConnection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListLocationsActivity extends AppCompatActivity {
    public ArrayList<Location> arrayOfLocations = new ArrayList<>();
    public LocationsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_locations);

        Intent intent = new Intent(this, MainActivity.class);

        ImageButton backButton = findViewById(R.id.BackButton);
        ImageButton mapButton = findViewById(R.id.MapButton);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        ListView listView = findViewById(R.id.lvLocations);
        adapter = new LocationsAdapter(this, arrayOfLocations);
        listView.setAdapter(adapter);

        executor.execute(() -> {
            DBFavorites db = new DBFavorites();
            //db.addFavorite(FirebaseConnection.getUser().getUid(), "2");
            //db.removeFavorite(FirebaseConnection.getUser().getUid(), "2");
            //db.getFavorites(FirebaseConnection.getUser().getUid());

            try {
                URL url = new URL   ("http://tp3.hexteckgate.ga/api.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK){
                    BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String jsonData = input.readLine();
                    input.close();

                    ArrayList<LinkedTreeMap<String, String>> jsonDataTable = makeIntoTable(jsonData);

                    handler.post(() -> {
                        for (LinkedTreeMap<String, String> tmp :
                                jsonDataTable) {
                            Location newLocation = new Location(tmp.get("title"), tmp.get("address"), tmp.get("coordinates"), tmp.get("description"),tmp.get("note"));
                            arrayOfLocations.add(newLocation);
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("Error : connexion", e.getLocalizedMessage());
            }
        });

        listView.setOnItemClickListener(
                (adapterView, view, i, l) -> {
                    Location location = null;
                    for (Location locationInArray:
                         arrayOfLocations) {
                        if ((String) ((TextView) view.findViewById(R.id.title)).getText() == locationInArray.getTitle()){
                            location = locationInArray;
                        }
                    }
                    intent.putExtra("location", location);
                    startActivity(intent);
                    System.out.println("Short Click");
                });

        listView.setOnItemLongClickListener(
                (adapterView, view, i, l) -> {



                    return true;
                }
        );

        backButton.setOnClickListener(view -> {
            Intent intentMapActivity = new Intent(this, MapActivity.class);
            startActivity(intentMapActivity);
        });


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        // Gérer l'événement de sélection pour l'élément Home
                        return true;
                    case R.id.navigation_map:
                        // Gérer l'événement de sélection pour l'élément Search
                        Intent map = new Intent(ListLocationsActivity.this, MapActivity.class);
                        map.putExtra("arrayOfLocations", arrayOfLocations);
                        startActivity(map);
                        return true;
                    case R.id.navigation_settings:
                        Intent intentRegisterActivity = new Intent(ListLocationsActivity.this, LandingActivity.class);
                        startActivity(intentRegisterActivity);
                        return true;
                    case R.id.navigation_logout:
                        Intent intentLogIn = new Intent(ListLocationsActivity.this, RegisterActivity.class);
                        startActivity(intentLogIn);
                        return true;
                }
                return false;
            }
        });
    }

    private ArrayList<LinkedTreeMap<String, String>> makeIntoTable(String json){
        return new Gson().fromJson(json, ArrayList.class);
    }
}