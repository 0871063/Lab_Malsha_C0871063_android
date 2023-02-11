package com.example.lab_malsha_c0871063_android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import com.example.lab_malsha_c0871063_android.databinding.ActivityMainBinding;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    ViewGroup root;
    private ActivityMainBinding binding;
    private PlaceListAdapter adapter;

    List<FavoritePlace> favoritePlaces;

    SharedPreferences sharedPreferences;

    public static final String SHARED_PREFERENCES_NAME = "Location";

    Gson gson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                buttonClicked();
            }
        });
        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        binding.placesListSearch.setOnQueryTextListener(this);

        binding.placesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                FavoritePlace place = favoritePlaces.get(position);
                intent.putExtra("Place", place);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaces();
        adapter.notifyDataSetChanged();
    }

    private void buttonClicked() {
        navigateToAdd();
    }

    private void navigateToAdd() {
        Intent intent = new Intent(getBaseContext(), MapsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String text = newText;
        adapter.filter(text);
        return false;
    }


    private void loadPlaces() {

        String receivedSerializedString = sharedPreferences.getString("location_serialized", null);

//        if (receivedSerializedString != null) {
//            favoritePlaces = gson.fromJson(receivedSerializedString, ArrayList.class);
//        }else{
//            favoritePlaces = new ArrayList<FavoritePlace>();
//        }

        try {
            if (receivedSerializedString != null) {
                favoritePlaces = (ArrayList<FavoritePlace>) ObjectSerializer.deserialize(receivedSerializedString);
            }else{
                favoritePlaces = new ArrayList<FavoritePlace>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        adapter = new PlaceListAdapter(this, R.layout.row_place_list_layout, favoritePlaces);
        binding.placesListView.setAdapter(adapter);
    }
}