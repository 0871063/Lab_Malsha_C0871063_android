package com.example.lab_malsha_c0871063_android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lab_malsha_c0871063_android.databinding.RowPlaceListLayoutBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlaceListAdapter extends ArrayAdapter {

    private static final String TAG = "PlaceListAdapter";
    private Context context;
    int resLayout;
    private List<FavoritePlace> placeInfoList;
    private ArrayList<FavoritePlace> arrayList;


    public PlaceListAdapter(@NonNull Context context, int resource, @NonNull List placeList) {
        super(context, resource, placeList);
        this.context = context;
        this.resLayout = resource;
        this.placeInfoList = placeList;
        this.arrayList = new ArrayList<FavoritePlace>();
        this.arrayList.addAll(placeList);
    }


    @Override
    public int getCount() {
        return this.placeInfoList.size();
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        RowPlaceListLayoutBinding binding = RowPlaceListLayoutBinding.inflate(LayoutInflater.from(context));


        final FavoritePlace place = placeInfoList.get(position);
        String address = place.getAddress();
        binding.addressTV.setText(address);
        binding.longitudeTV.setText(String.valueOf(place.getLongitude()));
        binding.latitudeTV.setText(String.valueOf(place.getLatitude()));
        binding.dateTV.setText(place.getDate());
        return binding.getRoot();
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        this.placeInfoList.clear();
        if (charText.length() == 0) {
            placeInfoList.addAll(arrayList);
        } else {
            for (FavoritePlace wp : arrayList) {
                if (wp.getAddress().toLowerCase(Locale.getDefault()).contains(charText)) {
                    placeInfoList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void loadPlaceList() {

        notifyDataSetChanged();
    }
}