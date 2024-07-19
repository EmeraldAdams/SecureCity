package com.example.finalproject;

import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalproject.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private SearchView mapSearchView;

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Bottom Navigation bar functionality
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = new MapsFragment();
            } else if (item.getItemId() == R.id.analytics) {
                selectedFragment = new AnalyticsFragment();
            } else if (item.getItemId() == R.id.alerts) {
                selectedFragment = new AlertsFragment();
            } else if (item.getItemId() == R.id.profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.MiddleSection, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Set the initial fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.MiddleSection, new MapsFragment())
                    .commit();
        }

        EdgeToEdge.enable(this);

        mapSearchView = findViewById(R.id.mapSearch);


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                if(location != null){
                    Geocoder geocoder = new Geocoder(MainActivity.this);
                    try{
                        addressList =geocoder.getFromLocationName(location,1);
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                    Address address = addressList.get(0);
                    LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                    myMap.addMarker(new MarkerOptions().position(latlng).title(location));
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,10));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


    }

    //For bottom navigation bar
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.MiddleSection, Fragment.class, null);
        fragmentTransaction.commit();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},FINE_PERMISSION_CODE);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location !=null){
                    currentLocation =location;


                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(MainActivity.this);
                }

            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap =googleMap;

        LatLng CapeTown = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        myMap.moveCamera(CameraUpdateFactory.newLatLng(CapeTown));
        MarkerOptions options = new MarkerOptions().position(CapeTown).title("My Location");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)); //color of marker we can change here
        myMap.addMarker(options);

    }
    public void onRequestPermissionResult(int requestCode,@NonNull String[] permission, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if(requestCode == FINE_PERMISSION_CODE){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else{
                Toast.makeText(this, "Location permission is denied, please allow permission", Toast.LENGTH_SHORT).show();
            }
        }
    }
}