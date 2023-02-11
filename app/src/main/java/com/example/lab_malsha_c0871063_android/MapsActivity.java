package com.example.lab_malsha_c0871063_android;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.lab_malsha_c0871063_android.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 1;
    Marker marker;
    Marker homeMarker;
    LocationManager locationManager;
    LocationListener locationListener;
    private ActivityMapsBinding binding;
    private Location currentLocation;

    SharedPreferences sharedPreferences;
    public static final String SHARED_PREFERENCES_NAME = "Location";
    private ArrayList<FavoritePlace> locations = new ArrayList<>();

    Gson gson = new Gson();

    FavoritePlace selectedLocationObj;

    Polyline line;

    boolean isSelectLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectedLocationObj = (FavoritePlace) getIntent().getSerializableExtra("Place");

        sharedPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        setMapViews();
        getLocations();
            SupportMapFragment mMapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().add(R.id.map, mMapFragment).commit();
            mMapFragment.getMapAsync(this);

            if (!Places.isInitialized()) {
                Places.initialize(this, getString(R.string.google_maps_key));
            }
            AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                    getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
            autocompleteFragment.getView().setBackgroundColor(Color.WHITE);
            autocompleteFragment.setPlaceFields(Arrays.asList(
                    Place.Field.ID,
                    Place.Field.NAME,
                    Place.Field.ADDRESS,
                    Place.Field.LAT_LNG));

            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(Place place) {
                    // TODO: Get info about the selected place.
                    Log.i("0", "Place: " + place.getName() + ", " + place.getId());
                    binding.locationBtn.setVisibility(View.VISIBLE);
                    if (selectedLocationObj == null) {
                        setMarker(place.getLatLng(), place.getName());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                    }
                }

                @Override
                public void onError(Status status) {
                    // TODO: Handle the error.
                    Log.i("0", "An error occurred: " + status);
                }
            });
    }

    private void setMapViews(){
        binding.btnHybridMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            }
        });

        binding.btnTerrainMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
            }
        });

        binding.btnSatelliteMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }else {
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                //show set current location.
                    setHomeMarker();
                if ( selectedLocationObj == null) { //pick from map
                    isSelectLocation = true;
                    binding.locationBtn.setVisibility(View.VISIBLE);
                    binding.distanceView.setVisibility(View.GONE);
                    binding.backBtn.setVisibility(View.VISIBLE);
                } else if (selectedLocationObj != null ) { //show in map
                    isSelectLocation = false;
                    setMarker(new LatLng(selectedLocationObj.getLatitude(), selectedLocationObj.getLongitude()), selectedLocationObj.getAddress());
                    getDistancePoint();
                    binding.locationBtn.setVisibility(View.GONE);
                    binding.distanceView.setVisibility(View.VISIBLE);
                    binding.backBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (!hasLocationPermission())
            requestLocationPermission();
        else
            startUpdateLocation();

        // apply long press gesture
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (selectedLocationObj == null) {
                    binding.locationBtn.setVisibility(View.VISIBLE);
                    binding.distanceView.setVisibility(View.GONE);
                    String address = getAddress(latLng);
                    setMarker(latLng, address);
                }
            }
        });

        binding.locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setCurrentLocationMarker();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void setMarker(LatLng latLng, String address) {

        clearMap();
        // set marker
        MarkerOptions options = new MarkerOptions().position(latLng)
                .title(address);

        marker = mMap.addMarker(options);
        setSelectLocation(latLng, address);

    }
    private void clearMap() {

        if (marker != null) {
            marker.remove();
            marker = null;
        }
        binding.distanceView.setVisibility(View.GONE);
        binding.locationBtn.setVisibility(View.VISIBLE);
    }

    private void setSelectLocation(LatLng latLng, String address) {
        // selected location
        if (isSelectLocation && selectedLocationObj == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Calendar c = Calendar.getInstance();
            String date = sdf.format(c.getTime());
            FavoritePlace locationInfo = new FavoritePlace(address, latLng.longitude, latLng.latitude, date);
           if (!locations.contains(locationInfo)) {
               locations.add(locationInfo);
               
//            String jsonString = gson.toJson(locations);
//            sharedPreferences.edit().putString("location_serialized", jsonString).apply();

               try {
                   sharedPreferences.edit().putString("location_serialized", ObjectSerializer.serialize((Serializable) locations)).apply();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
    }

    private void setHomeMarker() {
      if (homeMarker == null) {
          LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
          MarkerOptions options = new MarkerOptions().position(userLocation)
                  .title("You are here")
                  .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                  .snippet("Your Location");
          homeMarker = mMap.addMarker(options);
          mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
      }
    }

    private void setCurrentLocationMarker() throws IOException {
        LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        MarkerOptions options = new MarkerOptions().position(userLocation)
                .title("You are here")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Your Location");
        mMap.addMarker(options);
        setSelectLocation(userLocation, getAddress(userLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
    }

    private void startUpdateLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (REQUEST_CODE == requestCode) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
            }
        }
    }

    //Get The Location Address
    private String getAddress(LatLng latLng) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String address = date;

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                address = "";

                // street name
                if (addressList.get(0).getFeatureName() != null)
                    address += addressList.get(0).getFeatureName() + ",";
                if (addressList.get(0).getPremises() != null)
                    address += addressList.get(0).getPremises() + ",";
                if (addressList.get(0).getThoroughfare() != null)
                    address += addressList.get(0).getThoroughfare() + ",";
                if (addressList.get(0).getLocality() != null)
                    address += addressList.get(0).getLocality() + ",";
                if (addressList.get(0).getPostalCode() != null)
                    address += addressList.get(0).getPostalCode() + ",";
                if (addressList.get(0).getAdminArea() != null)
                    address += addressList.get(0).getAdminArea();
                if (addressList.get(0).getCountryName() != null)
                    address += addressList.get(0).getCountryName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return address;
    }

    //Get Distance
    public double calculateDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double distance = Radius * c;
        double kmDistance = distance / 1;
        double meterDistance = distance % 1000;
        return meterDistance;
    }

    private void getDistancePoint(){
        String displayString = "" ;

        if ( selectedLocationObj != null) {
            String duration = getDuration();
            LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            LatLng location = new LatLng(selectedLocationObj.getLatitude(), selectedLocationObj.getLongitude());
            Double distanceAB = calculateDistance(userLocation, location);
            displayString = "Distance between location : " + String.format("%.2f", distanceAB) + " m. \n";
            displayString = displayString + duration;

            binding.locationBtn.setVisibility(View.GONE);
            binding.distanceView.setVisibility(View.VISIBLE);
            binding.distanceText.setText(displayString);
            drawLine(userLocation,location);
        }
    }
    
    //Shared Preference

    private void getLocations() {

        String receivedSerializedString = sharedPreferences.getString("location_serialized", null);

//        if (receivedSerializedString != null) {
//            locations = gson.fromJson(receivedSerializedString, ArrayList.class);
//        }else{
//            locations = new ArrayList<FavoritePlace>();
//        }

        try {
            if (receivedSerializedString != null) {
               locations = (ArrayList<FavoritePlace>) ObjectSerializer.deserialize(receivedSerializedString);
            }else{
                locations = new ArrayList<FavoritePlace>();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Draw Line
    private void drawLine(LatLng userLocation, LatLng selectedLocation) {
        PolylineOptions polyline = new PolylineOptions().width(5)
                .color(Color.RED);

        polyline.add(userLocation,selectedLocation);
        line = mMap.addPolyline(polyline);
    }

    //Get Duration
    private String getDuration(){
        String durationString = "";
        if ( selectedLocationObj != null) {
            LatLng userLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            LatLng location = new LatLng(selectedLocationObj.getLatitude(), selectedLocationObj.getLongitude());
            Double distanceAB = calculateDistance(userLocation, location);
            durationString = "Duration between location : " ;
        }
        return  durationString;
    }
}
