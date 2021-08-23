package com.example.memorableplaces;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity1 extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    LocationManager locationManager;
    LocationListener locationListener;
    private GoogleMap mMap;

    public void centerMapOnLocation(Location location, String title){
        if(location!= null) {
            LatLng user = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(user).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(user, 8));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnown, "Your Location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);


        Intent intent = getIntent();
        if(intent.getIntExtra("place",0) == 0){
            //ZOOm on user location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    centerMapOnLocation(location,"Your Location!");
                }
            };

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnown, "Your Location");
            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else {
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(intent.getIntExtra("place",0)).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(intent.getIntExtra("place",0)).longitude);

            centerMapOnLocation(placeLocation,MainActivity.places.get(intent.getIntExtra("place",0)));
        }

    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addressList != null && addressList.size() > 0) {

                if (addressList.get(0).getThoroughfare() != null) {
                    address += addressList.get(0).getThoroughfare() + " ";
                }

                if (addressList.get(0).getLocality() != null) {
                    address += addressList.get(0).getLocality();
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        if(address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss yyyy-MM-dd");
            address += sdf.format(new Date());
        }

        //mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);
        MainActivity.arrayAdapter.notifyDataSetChanged();

        Toast.makeText(this,"Location Saved",Toast.LENGTH_SHORT).show();
    }
}